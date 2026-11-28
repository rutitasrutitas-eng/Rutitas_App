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


    /** Cambia detalles sin sobrescribir las plazas disminuidas por otras operaciones. */

    /**
     * ANDROID-005: edicion con turno y tipo definidos por el
     * conductor propietario. Guarda todos los campos mediante
     * una unica sentencia SQL protegida.
     *
     * Metodo candidato: se conectara al formulario cuando
     * finalicen las pruebas y se elimine el camino anterior.
     */
    /**
     * ANDROID-005: publica una ruta con clasificacion explicita.
     * El turno, la direccion y el horario son conceptos separados.
     * Este metodo se conectara posteriormente al formulario.
     */
    suspend fun publishClassified(
        origin: String,
        destination: String,
        departure: String,
        seats: Int,
        price: Int,
        days: String,
        recurring: Boolean,
        waitMinutes: Int,
        shift: Shift,
        direction: TripDirection,
        stops: List<String>? = null
    ): Result<Long> {
        val snapshot = session.current

        val user = snapshot.user
            ?: return Result.failure(
                IllegalStateException("Inicia sesion para publicar.")
            )

        if (snapshot.isDisabled ||
            snapshot.driverStatus != DriverStatus.APPROVED
        ) {
            return Result.failure(
                IllegalStateException("Necesitas aprobacion como conductor.")
            )
        }

        if (origin.isBlank() ||
            destination.isBlank() ||
            origin.trim().equals(destination.trim(), ignoreCase = true) ||
            seats <= 0 ||
            price <= 0 ||
            days.isBlank() ||
            waitMinutes < 0
        ) {
            return Result.failure(
                IllegalArgumentException("Datos de ruta invalidos.")
            )
        }

        val dep = TimeUtils.toMinutes(departure)
            ?: return Result.failure(
                IllegalArgumentException("Hora invalida.")
            )

        delay(400)

        if (session.current.user?.id != user.id ||
            session.current.isDisabled ||
            session.current.driverStatus != DriverStatus.APPROVED
        ) {
            return Result.failure(
                IllegalStateException("La autorizacion cambio.")
            )
        }

        val route = RouteEntity(
            origin = origin.trim(),
            destination = destination.trim(),
            departure = TimeUtils.format(dep),
            arrival = TimeUtils.format(dep + 30),
            seats = seats,
            price = price,
            driverName = user.name,
            driverRating = 4.8,
            car = snapshot.vehicle.description.ifBlank { "Mi auto" },
            stops = (
                stops ?: defaultStops(origin.trim(), destination.trim())
            ).joinToString("|"),
            status = RouteStatus.ACTIVE.name,
            ownedByMe = true,
            alternative = false,
            tag = null,
            days = days,
            recurring = recurring,
            waitMinutes = waitMinutes,
            ownerUserId = user.id,
            shift = shift.name,
            direction = direction.name
        )

        val id = dao.insert(route)
        return Result.success(id)
    }
    suspend fun updateClassified(
        id: Long,
        origin: String,
        destination: String,
        departure: String,
        seats: Int,
        price: Int,
        days: String,
        recurring: Boolean,
        waitMinutes: Int,
        shift: Shift,
        direction: TripDirection,
        stops: List<String>? = null
    ): Result<Unit> {
        val snapshot = session.current

        val owner = snapshot.user?.id
            ?: return Result.failure(
                IllegalStateException("Inicia sesion para editar.")
            )

        if (snapshot.isDisabled ||
            snapshot.driverStatus != DriverStatus.APPROVED
        ) {
            return Result.failure(
                IllegalStateException("Necesitas autorizacion como conductor.")
            )
        }

        if (seats < 0 || price <= 0 ||
            origin.isBlank() || destination.isBlank() ||
            origin.trim().equals(destination.trim(), ignoreCase = true)
        ) {
            return Result.failure(
                IllegalArgumentException("Datos de ruta invalidos.")
            )
        }

        val current = dao.get(id)
            ?: return Result.failure(
                IllegalStateException("No encontramos la ruta.")
            )

        if (!ownedBy(current, owner)) {
            return Result.failure(
                IllegalStateException("Esta ruta no es tuya.")
            )
        }

        if (current.status !in listOf(
                RouteStatus.ACTIVE.name,
                RouteStatus.INTERRUPTED.name
            )
        ) {
            return Result.failure(
                IllegalStateException("Esta ruta no admite edicion.")
            )
        }

        val dep = TimeUtils.toMinutes(departure)
            ?: return Result.failure(
                IllegalArgumentException("Hora invalida.")
            )

        val newStops = (
            stops ?: if (
                origin == current.origin &&
                destination == current.destination
            ) {
                current.stops.split("|")
            } else {
                defaultStops(origin, destination)
            }
        ).joinToString("|")

        delay(300)

        if (session.current.user?.id != owner ||
            session.current.isDisabled ||
            session.current.driverStatus != DriverStatus.APPROVED
        ) {
            return Result.failure(
                IllegalStateException("La autorizacion cambio.")
            )
        }

        val changed = dao.updateRouteWithClassificationIfSafe(
            id = id,
            ownerId = owner,
            origin = origin,
            destination = destination,
            departure = TimeUtils.format(dep),
            arrival = TimeUtils.format(dep + 30),
            seats = seats,
            price = price,
            days = days,
            recurring = recurring,
            waitMinutes = waitMinutes,
            stops = newStops,
            shift = shift.name,
            direction = direction.name
        )

        if (changed != 1) {
            return Result.failure(
                IllegalStateException(
                    "La ruta tiene reservas, cambio de estado " +
                    "o no admite esta modificacion."
                )
            )
        }

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
