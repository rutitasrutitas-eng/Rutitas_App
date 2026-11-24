package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.RouteEntity
import mx.edu.uttt.rutitas.database.toDomain
import mx.edu.uttt.rutitas.domain.Recommender
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.network.ApiService
import mx.edu.uttt.rutitas.utils.AppConfig
import mx.edu.uttt.rutitas.utils.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val dao: RouteDao,
    private val api: ApiService,
    private val session: SessionManager,
    private val bookingDao: BookingDao
) {
    private fun ownedBy(r: RouteEntity, userId: Long?) = userId != null && r.ownerUserId == userId
    private fun RouteEntity.forSession(userId: Long?) = toDomain().copy(ownedByMe = ownedBy(this, userId))

    /** Rutas visibles al pasajero, ordenadas según su horario y zona. */
    val publicRoutes: Flow<List<Route>> = combine(dao.observeAll(), session.state) { routes, s ->
        Recommender.rank(
            routes.filter { !ownedBy(it, s.user?.id) && !it.alternative &&
                it.status != RouteStatus.CANCELLED.name && it.status != RouteStatus.FINISHED.name }
                .map { it.forSession(s.user?.id) },
            s
        )
    }

    val myRoutes: Flow<List<Route>> = combine(dao.observeAll(), session.state) { rows, s ->
        rows.filter { ownedBy(it, s.user?.id) }.map { it.forSession(s.user?.id) }
    }

    fun observe(id: Long): Flow<Route?> = combine(dao.observe(id), bookingDao.observeAll(), session.state) { r, bookings, s ->
        val userId = s.user?.id
        val hasBooking = userId != null && bookings.any { b -> b.routeId == id &&
            (b.ownerUserId == userId || (userId == 0L && b.seeded && b.ownerUserId == null)) }
        r?.takeIf { ownedBy(it, userId) || hasBooking ||
            it.status in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name, RouteStatus.INTERRUPTED.name) }
            ?.forSession(userId)
    }

    fun alternatives(excludeId: Long): Flow<List<Route>> = combine(dao.observeAll(), session.state) { rows, s ->
        rows.filter { !ownedBy(it, s.user?.id) && it.id != excludeId &&
            it.status == RouteStatus.ACTIVE.name && it.seats > 0 }
            .sortedByDescending { it.alternative }.map { it.forSession(s.user?.id) }
    }

    /** Punto de enlace con el backend real: si DEMO_MODE=false descarga rutas y las guarda en Room. */
    suspend fun refreshFromServer() {
        if (AppConfig.DEMO_MODE) return
        runCatching { api.routes() }.onSuccess { dtos -> dao.insertAll(dtos.map { it.toEntity() }) }
    }

    suspend fun publish(
        origin: String, destination: String, departure: String, seats: Int, price: Int,
        days: String, recurring: Boolean, waitMinutes: Int = 5, stops: List<String>? = null
    ): Result<Long> {
        val s = session.current
        if (s.isDisabled) return Result.failure(IllegalStateException("Tu cuenta está inhabilitada. No puedes publicar rutas."))
        if (s.user == null) return Result.failure(IllegalStateException("Inicia sesión para publicar."))
        val dep = TimeUtils.toMinutes(departure) ?: return Result.failure(IllegalArgumentException("Hora inválida"))
        delay(400)
        if (session.current.user?.id != s.user.id) return Result.failure(IllegalStateException("La sesión cambió."))
        val id = dao.insert(
            RouteEntity(
                origin = origin, destination = destination, departure = TimeUtils.format(dep),
                arrival = TimeUtils.format(dep + 30), price = price, seats = seats,
                driverName = s.user.name, driverRating = 4.8,
                car = s.vehicle.description.ifBlank { "Mi auto" },
                stops = (stops ?: defaultStops(origin, destination)).joinToString("|"),
                status = RouteStatus.ACTIVE.name, ownedByMe = true, alternative = false, tag = null,
                days = days, recurring = recurring, waitMinutes = waitMinutes, ownerUserId = s.user.id
            )
        )
        return Result.success(id)
    }

    /** Cambia detalles sin sobrescribir las plazas disminuidas por otras operaciones. */
    suspend fun update(
        id: Long, origin: String, destination: String, departure: String, seats: Int, price: Int,
        days: String, recurring: Boolean, waitMinutes: Int, stops: List<String>? = null
    ): Result<Unit> {
        val snapshot = session.current
        val owner = snapshot.user?.id
            ?: return Result.failure(IllegalStateException("Inicia sesion para editar."))
        if (snapshot.isDisabled) return Result.failure(IllegalStateException("Cuenta inhabilitada."))
        if (seats < 0) return Result.failure(IllegalArgumentException("Los asientos no pueden ser negativos."))
        val current = dao.get(id)
            ?: return Result.failure(IllegalStateException("No encontramos la ruta."))
        if (!ownedBy(current, owner)) return Result.failure(IllegalStateException("Esta ruta no es tuya."))
        if (current.status !in listOf(RouteStatus.ACTIVE.name, RouteStatus.INTERRUPTED.name))
            return Result.failure(IllegalStateException("Esta ruta ya no admite edicion."))
        val dep = TimeUtils.toMinutes(departure)
            ?: return Result.failure(IllegalArgumentException("Hora invalida."))
        delay(300)
        if (session.current.user?.id != owner || session.current.isDisabled)
            return Result.failure(IllegalStateException("La sesion cambio durante la edicion."))
        val newStops = (stops ?: if (origin == current.origin && destination == current.destination)
            current.stops.split("|") else defaultStops(origin, destination)).joinToString("|")
        val changed = dao.updateDetailsIfSafe(
            id, owner, origin, destination, TimeUtils.format(dep), TimeUtils.format(dep + 30),
            seats, price, days, recurring, waitMinutes, newStops
        )
        if (changed != 1)
            return Result.failure(IllegalStateException(
                "La ruta o sus asientos cambiaron. Actualiza los datos antes de guardar."
            ))
        return Result.success(Unit)
    }

    private fun defaultStops(origin: String, destination: String): List<String> =
        listOf(if (origin.endsWith("Centro")) origin else "$origin Centro", destination)

    suspend fun setStatus(id: Long, status: RouteStatus) {
        val userId = session.current.user?.id ?: return
        if (session.current.isDisabled) return

        if (status != RouteStatus.ACTIVE &&
            status != RouteStatus.INTERRUPTED) return

        dao.changeStatusIfAllowed(id, userId, status.name)
    }
}
