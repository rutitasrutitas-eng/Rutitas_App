package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val photoStore: PrivatePhotoStore
) {
    val requests: Flow<List<RideRequest>> = requestDao.observeAll().map { l -> l.map { it.toDomain() } }

    private val _earnings = MutableStateFlow(DemoData.earnings())
    val earnings: StateFlow<EarningsState> = _earnings.asStateFlow()

    private val _incidents = MutableStateFlow(DemoData.incidents())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    /** Fotos privadas del registro: "identity", "vehicle", "doc_LICENCIA"... */
    private val _photos = MutableStateFlow<Map<String, File>>(emptyMap())
    val photos: StateFlow<Map<String, File>> = _photos.asStateFlow()

    suspend fun setPhoto(key: String, source: Any) {
        val f = withContext(Dispatchers.IO) { photoStore.save(key, source) }
        if (f != null) _photos.update { it + (key to f) }
    }

    fun removePhoto(key: String) {
        photoStore.delete(key)
        _photos.update { it - key }
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
        if (routeDao.ownedCount() == 0) {
            routeDao.insert(DemoData.ownRoute(s.user?.name ?: "Conductor", s.vehicle.description))
        }
    }

    // ---------- Solicitudes ----------

    /** Devuelve null si todo salió bien, o el mensaje de error. */
    suspend fun acceptRequest(id: Long, routeId: Long? = null): String? {
        if (session.current.isDisabled) return "Tu cuenta está inhabilitada. No puedes aceptar solicitudes."
        val req = requestDao.get(id) ?: return "No encontramos la solicitud."
        val route = (if (routeId != null) routeDao.get(routeId) else routeDao.firstOwnedActive())
            ?: return "Necesitas una ruta activa para aceptar pasajeros."
        if (route.status != RouteStatus.ACTIVE.name && route.status != RouteStatus.IN_PROGRESS.name) {
            return "Esta ruta ya no acepta pasajeros."
        }
        if (route.seats < req.seats) return "No tienes lugares disponibles."
        routeDao.update(route.copy(seats = route.seats - req.seats))
        requestDao.update(req.copy(status = RequestStatus.ACCEPTED.name, routeId = route.id))
        _earnings.update {
            val amount = route.price * req.seats
            it.copy(
                month = it.month + amount, trips = it.trips + 1,
                movements = listOf(Earning("${route.origin} → ${route.destination} · ${req.passengerName}", amount, "Hoy")) + it.movements
            )
        }
        return null
    }

    suspend fun rejectRequest(id: Long) {
        requestDao.get(id)?.let { requestDao.update(it.copy(status = RequestStatus.REJECTED.name)) }
    }

    /** El conductor valida el código que le muestra el pasajero para abordar. */
    suspend fun validateCode(routeId: Long, raw: String): Result<String> {
        if (session.current.isDisabled) return Result.failure(IllegalStateException("Tu cuenta está inhabilitada."))
        val code = raw.trim().uppercase()
        if (code.isBlank()) return Result.failure(IllegalArgumentException("Escribe el código del pasajero."))
        val req = requestDao.findByCode(code)
        if (req != null) {
            if (req.status != RequestStatus.ACCEPTED.name || req.routeId != routeId) {
                return Result.failure(IllegalStateException("Este código no pertenece a un pasajero aceptado en esta ruta."))
            }
            if (req.validated) return Result.failure(IllegalStateException("${req.passengerName} ya fue validado."))
            requestDao.update(req.copy(validated = true))
            return Result.success("Código válido: ${req.passengerName} ya puede abordar.")
        }
        // Demo: reservas hechas desde el modo pasajero en este mismo dispositivo
        val b = bookingDao.findByCode(code)
        if (b != null && b.status == BookingStatus.CONFIRMED.name) {
            return Result.success("Código válido: reserva ${b.code} (${b.origin} → ${b.destination}, ${b.seats} lugar(es)).")
        }
        return Result.failure(IllegalStateException("Código no válido."))
    }

    // ---------- Acciones de ruta ----------

    suspend fun startRoute(id: Long): String? {
        if (session.current.isDisabled) return "Tu cuenta está inhabilitada. No puedes iniciar rutas."
        val r = routeDao.get(id) ?: return "No encontramos la ruta."
        if (r.status != RouteStatus.ACTIVE.name) return "Solo puedes iniciar una ruta activa."
        routeDao.update(r.copy(status = RouteStatus.IN_PROGRESS.name, progress = 1))
        return null
    }

    suspend fun advanceRoute(id: Long, stageCount: Int) {
        val r = routeDao.get(id) ?: return
        routeDao.update(r.copy(progress = (r.progress + 1).coerceAtMost(stageCount), waitStartedAt = 0L))
    }

    /** El conductor llegó al punto de encuentro: empieza a correr el tiempo de espera. */
    suspend fun startWait(id: Long) {
        val r = routeDao.get(id) ?: return
        routeDao.update(r.copy(waitStartedAt = System.currentTimeMillis()))
    }

    /** Termina la espera antes de tiempo (el pasajero ya abordó o no se presentó). */
    suspend fun endWait(id: Long) {
        val r = routeDao.get(id) ?: return
        routeDao.update(r.copy(waitStartedAt = 0L))
    }

    suspend fun finishRoute(id: Long) {
        val r = routeDao.get(id) ?: return
        routeDao.update(r.copy(status = RouteStatus.FINISHED.name))
    }

    /** Cancela el viaje: la ruta pasa a Cancelada y se avisa (simulado) a los pasajeros aceptados. */
    suspend fun cancelRoute(id: Long) {
        val r = routeDao.get(id) ?: return
        routeDao.update(r.copy(status = RouteStatus.CANCELLED.name))
        requests.first().filter { it.routeId == id && it.status == RequestStatus.ACCEPTED }.forEach { q ->
            requestDao.get(q.id)?.let { requestDao.update(it.copy(status = RequestStatus.CANCELLED.name)) }
        }
    }

    fun resetDemoState() {
        _earnings.value = DemoData.earnings()
        _incidents.value = DemoData.incidents()
        photoStore.deleteAll()
        _photos.value = emptyMap()
    }

    fun addIncident(i: Incident) = _incidents.update { listOf(i) + it }
}
