package mx.edu.uttt.rutitas.repository

import androidx.room.withTransaction
import mx.edu.uttt.rutitas.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.RequestDao
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.database.toDomain
import mx.edu.uttt.rutitas.model.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val requestDao: RequestDao,
    private val bookingDao: BookingDao,
    private val session: SessionManager,
    private val photoStore: PrivatePhotoStore,
    private val db: AppDatabase
) {
    private fun visibleRequest(ownerId: Long?, routeId: Long, status: String, userId: Long?) =
        userId != null && (ownerId == userId ||
            (ownerId == null && routeId == 0L && status == RequestStatus.PENDING.name))

    val requests: Flow<List<RideRequest>> = combine(requestDao.observeAll(), session.state) { rows, s ->
        rows.filter { visibleRequest(it.ownerUserId, it.routeId, it.status, s.user?.id) }
            .map { it.toDomain() }
    }

    private val _earnings = MutableStateFlow(DemoData.earnings())
    val earnings: StateFlow<EarningsState> = _earnings.asStateFlow()

    private val _incidents = MutableStateFlow(DemoData.incidents())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    /** Fotos privadas del registro: "identity", "vehicle", "doc_LICENCIA"... */
    private val _photos = MutableStateFlow<Map<String, File>>(emptyMap())
    // La UI ve solamente las fotos de la sesión actual, incluso si cambia de cuenta.
    val photos: Flow<Map<String, File>> = combine(_photos, session.state) { all, s ->
        val prefix = "${s.user?.id ?: "no-session"}:"
        all.filterKeys { it.startsWith(prefix) }.mapKeys { it.key.removePrefix(prefix) }
    }

    private fun photoKey(userId: Long, kind: String) = "user_${userId}_${kind}"

    suspend fun setPhoto(key: String, source: Any) {
        val userId = session.current.user?.id ?: return
        val f = withContext(Dispatchers.IO) { photoStore.save(photoKey(userId, key), source) }
        if (f != null) _photos.update { it + ("$userId:$key" to f) }
    }

    fun removePhoto(key: String) {
        val userId = session.current.user?.id ?: return
        photoStore.delete(photoKey(userId, key))
        _photos.update { it - "$userId:$key" }
    }

    /** Usado por baja de cuenta: nunca elimina archivos de otros usuarios. */
    fun removeUserPhotos(userId: Long) {
        photoStore.deleteForUser(userId)
        _photos.update { it.filterKeys { storedKey -> !storedKey.startsWith("$userId:") } }
    }

    fun startOnboarding() = session.update {
        if (it.driverStatus == DriverStatus.NONE) it.copy(driverStatus = DriverStatus.IN_PROGRESS) else it
    }

    fun saveVehicle(v: Vehicle) = session.update { it.copy(vehicle = v) }

    suspend fun setDoc(kind: DocKind, photo: Any?) {
        if (photo != null) setPhoto("doc_${kind.name}", photo)
        session.update { it.copy(documents = it.documents + (kind to DocStatus.COMPLETED)) }
    }

    fun completeAllDocs() = session.update { s -> s.copy(documents = DocKind.entries.associateWith { DocStatus.COMPLETED }) }

    fun submitForReview() = session.update { it.copy(driverStatus = DriverStatus.IN_REVIEW) }

    suspend fun approve() {
        val s = session.current
        session.update { it.copy(driverStatus = DriverStatus.APPROVED) }
        val userId = s.user?.id ?: return
        if (routeDao.byOwner(userId).isEmpty()) {
            routeDao.insert(DemoData.ownRoute(s.user?.name ?: "Conductor", s.vehicle.description).copy(ownerUserId = userId))
        }
    }

    // ---------- Solicitudes ----------

    /** Devuelve null si todo salió bien, o el mensaje de error. */
    suspend fun acceptRequest(id: Long, routeId: Long? = null): String? {
        val userId = session.current.user?.id ?: return "Inicia sesión primero."
        if (session.current.isDisabled) return "Tu cuenta está inhabilitada. No puedes aceptar solicitudes."
        val outcome = db.withTransaction {
            val req = requestDao.get(id) ?: return@withTransaction "No encontramos la solicitud." to null
            if (!visibleRequest(req.ownerUserId, req.routeId, req.status, userId))
                return@withTransaction "Esta solicitud no está dirigida a tu cuenta." to null
            if (req.status != RequestStatus.PENDING.name) return@withTransaction "Esta solicitud ya fue atendida." to null
            if (req.seats <= 0) return@withTransaction "La cantidad de asientos no es válida." to null
            val route = (if (routeId != null) routeDao.get(routeId) else routeDao.byOwner(userId).firstOrNull {
                it.status == RouteStatus.ACTIVE.name
            }) ?: return@withTransaction "Necesitas una ruta activa para aceptar pasajeros." to null
            if (route.ownerUserId != userId) return@withTransaction "Esta ruta no es tuya." to null
            if (route.status != RouteStatus.ACTIVE.name && route.status != RouteStatus.IN_PROGRESS.name)
                return@withTransaction "Esta ruta ya no acepta pasajeros." to null
            if (routeDao.decrementSeatsIfAvailable(route.id, req.seats) != 1)
                return@withTransaction "No tienes lugares disponibles." to null
            if (session.current.user?.id != userId) error("La sesión cambió durante la aceptación.")
            if (requestDao.acceptIfPending(id, route.id, userId) != 1)
                error("No se pudo registrar la aceptación de la solicitud.")
            null to Earning("${route.origin} → ${route.destination} · ${req.passengerName}", route.price * req.seats, "Hoy")
        }
        val (error, earning) = outcome
        if (earning != null) _earnings.update {
            it.copy(month = it.month + earning.amount, trips = it.trips + 1,
                movements = listOf(earning) + it.movements)
        }
        return error
    }

    suspend fun rejectRequest(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            requestDao.get(id)?.takeIf { it.ownerUserId == userId && it.status == RequestStatus.PENDING.name }
                ?.let { requestDao.update(it.copy(status = RequestStatus.REJECTED.name)) }
        }
    }

    /** El conductor valida el código que le muestra el pasajero para abordar. */
    suspend fun validateCode(routeId: Long, raw: String): Result<String> {
        if (session.current.isDisabled) return Result.failure(IllegalStateException("Tu cuenta está inhabilitada."))
        val userId = session.current.user?.id
            ?: return Result.failure(IllegalStateException("Inicia sesión primero."))
        val code = raw.trim().uppercase()
        if (code.isBlank()) return Result.failure(IllegalArgumentException("Escribe el código del pasajero."))
        return db.withTransaction {
            if (session.current.user?.id != userId)
                return@withTransaction Result.failure(IllegalStateException("La sesión cambió."))
            val route = routeDao.get(routeId)
            if (route?.ownerUserId != userId || route.status !in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name)) {
                return@withTransaction Result.failure(IllegalStateException("Esta ruta no es tuya o no está vigente."))
            }
            val req = requestDao.findByCode(code)
            if (req != null) {
                if (requestDao.markValidatedIfAccepted(req.id, routeId, userId) != 1)
                    return@withTransaction Result.failure(IllegalStateException("Código ajeno, vencido o ya validado."))
                return@withTransaction Result.success("Código válido: ${req.passengerName} ya puede abordar.")
            }
            val b = bookingDao.findByCode(code)
            if (b != null && bookingDao.markBoardedIfValid(b.id, routeId) == 1) {
                return@withTransaction Result.success("Código válido: reserva ${b.code} (${b.origin} → ${b.destination}, ${b.seats} lugar(es)).")
            }
            Result.failure(IllegalStateException("Código no válido, de otra ruta o ya validado."))
        }
    }

    // ---------- Acciones de ruta ----------

    suspend fun startRoute(id: Long): String? {
        if (session.current.isDisabled) return "Tu cuenta está inhabilitada. No puedes iniciar rutas."
        val userId = session.current.user?.id ?: return "Inicia sesión primero."
        val r = routeDao.get(id) ?: return "No encontramos la ruta."
        if (r.ownerUserId != userId) return "Esta ruta no es tuya."
        if (r.status != RouteStatus.ACTIVE.name) return "Solo puedes iniciar una ruta activa."
        routeDao.update(r.copy(status = RouteStatus.IN_PROGRESS.name, progress = 1))
        return null
    }

    suspend fun advanceRoute(id: Long, stageCount: Int) {
        val userId = session.current.user?.id ?: return
        val r = routeDao.get(id)?.takeIf { it.ownerUserId == userId && it.status == RouteStatus.IN_PROGRESS.name } ?: return
        if (stageCount <= 0) return
        routeDao.update(r.copy(progress = (r.progress + 1).coerceAtMost(stageCount), waitStartedAt = 0L))
    }

    /** El conductor llegó al punto de encuentro: empieza a correr el tiempo de espera. */
    suspend fun startWait(id: Long) {
        val userId = session.current.user?.id ?: return
        val r = routeDao.get(id)?.takeIf { it.ownerUserId == userId && it.status == RouteStatus.IN_PROGRESS.name } ?: return
        routeDao.update(r.copy(waitStartedAt = System.currentTimeMillis()))
    }

    /** Termina la espera antes de tiempo (el pasajero ya abordó o no se presentó). */
    suspend fun endWait(id: Long) {
        val userId = session.current.user?.id ?: return
        val r = routeDao.get(id)?.takeIf { it.ownerUserId == userId && it.status == RouteStatus.IN_PROGRESS.name } ?: return
        routeDao.update(r.copy(waitStartedAt = 0L))
    }

    suspend fun finishRoute(id: Long) {
        val userId = session.current.user?.id ?: return
        val r = routeDao.get(id)?.takeIf { it.ownerUserId == userId && it.status == RouteStatus.IN_PROGRESS.name } ?: return
        routeDao.update(r.copy(status = RouteStatus.FINISHED.name))
    }

    /** Cancela el viaje: la ruta pasa a Cancelada y se avisa (simulado) a los pasajeros aceptados. */
    suspend fun cancelRoute(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            val r = routeDao.get(id)?.takeIf { it.ownerUserId == userId &&
                it.status in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name, RouteStatus.INTERRUPTED.name) }
                ?: return@withTransaction
            routeDao.update(r.copy(status = RouteStatus.CANCELLED.name))
            requestDao.cancelAcceptedForRoute(id, userId)
        }
    }

    fun resetDemoState() {
        _earnings.value = DemoData.earnings()
        _incidents.value = DemoData.incidents()
        // No limpiar archivos de otros usuarios durante un reinicio de DEMO.
        _photos.value = emptyMap()
    }

    fun addIncident(i: Incident) = _incidents.update { listOf(i) + it }
}
