package mx.edu.uttt.rutitas.repository

import androidx.room.withTransaction
import mx.edu.uttt.rutitas.database.AppDatabase
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.BookingEntity
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.database.toDomain
import mx.edu.uttt.rutitas.model.Booking
import mx.edu.uttt.rutitas.model.BookingStatus
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.TripDirection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val dao: BookingDao,
    private val routeDao: RouteDao,
    private val session: SessionManager,
    private val db: AppDatabase
) {
    private fun visible(b: BookingEntity, userId: Long?) =
        userId != null && (b.ownerUserId == userId || (userId == 0L && b.seeded && b.ownerUserId == null))

    val bookings: Flow<List<Booking>> = combine(dao.observeAll(), session.state) { rows, s ->
        rows.filter { visible(it, s.user?.id) }.map { it.toDomain() }
    }

    /**
     * Reservas confirmadas de una ruta propia.
     * Se valida propiedad y autorizacion en el repositorio,
     * no solamente en la interfaz.
     */
    fun confirmedForDriverRoute(routeId: Long): Flow<List<Booking>> =
        combine(dao.observeAll(), routeDao.observe(routeId), session.state) { rows, route, s ->
            val userId = s.user?.id

            if (userId == null ||
                s.isDisabled ||
                s.driverStatus != DriverStatus.APPROVED ||
                route == null ||
                route.ownerUserId != userId
            ) {
                emptyList()
            } else {
                rows.filter {
                    it.routeId == routeId &&
                        it.status == BookingStatus.CONFIRMED.name
                }.map { it.toDomain() }
            }
        }
    /** Reservas activas cuya ruta fue interrumpida (notificación simulada al pasajero). */
    val interrupted: Flow<List<Booking>> = combine(bookings, routeDao.observeAll()) { bs, rs ->
        val broken = rs.filter { it.status == RouteStatus.INTERRUPTED.name }.map { it.id }.toSet()
        bs.filter { it.routeId in broken && (it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.PENDING) }
    }

    fun observe(id: Long): Flow<Booking?> = combine(dao.observe(id), session.state) { b, s ->
        b?.takeIf { visible(it, s.user?.id) }?.toDomain()
    }

    private fun fail(msg: String) = Result.failure<Long>(IllegalStateException(msg))

    suspend fun reserve(
        routeId: Long, pickup: String, seats: Int,
        shift: Shift = Shift.MATUTINO, direction: TripDirection = TripDirection.ENTRADA
    ): Result<Long> {
        val s = session.current
        val owner = s.user?.id ?: return fail("Inicia sesión para reservar.")
        if (s.isDisabled) return fail("Tu cuenta está inhabilitada. No puedes reservar.")
        if (seats <= 0) return fail("Solicita al menos un asiento.")
        if (pickup.isBlank()) return fail("Selecciona un punto de encuentro.")
        return try {
            db.withTransaction {
                if (session.current.user?.id != owner || session.current.isDisabled) return@withTransaction fail("La sesión cambió o la cuenta está inhabilitada.")
                val route = routeDao.get(routeId) ?: return@withTransaction fail("No encontramos la ruta.")
                if (route.status != RouteStatus.ACTIVE.name)
                    return@withTransaction fail("Esta ruta no admite reservas nuevas.")
                val routeShift = route.shift
                    ?.let { runCatching { Shift.valueOf(it) }.getOrNull() }
                    ?: return@withTransaction fail(
                        "El conductor debe completar el turno de esta ruta."
                    )

                val routeDirection = route.direction
                    ?.let { runCatching { TripDirection.valueOf(it) }.getOrNull() }
                    ?: return@withTransaction fail(
                        "El conductor debe completar el tipo de viaje."
                    )

                val validStops = route.stops.split("|").map { it.trim() }
                if (pickup.trim() !in validStops) {
                    return@withTransaction fail(
                        "Selecciona un punto de encuentro de la ruta."
                    )
                }

                if (seats > route.seats)
                    return@withTransaction fail("Solo quedan ${route.seats} lugares.")
                if (routeDao.decrementSeatsIfAvailable(route.id, seats) != 1)
                    return@withTransaction fail("No hay suficientes asientos disponibles.")
                if (session.current.user?.id != owner || session.current.isDisabled) error("La sesión cambió o la cuenta está inhabilitada durante la reserva.")
                val id = dao.insert(
                    BookingEntity(
                        code = "RT-${UUID.randomUUID()}".uppercase(), routeId = route.id,
                        origin = route.origin, destination = route.destination,
                        departure = route.departure, pickup = pickup, driverName = route.driverName,
                        car = route.car, seats = seats, total = route.price * seats,
                        status = BookingStatus.CONFIRMED.name, progress = 0, rated = false,
                        seeded = false, dateLabel = "Hoy", shift = routeShift.name,
                        direction = routeDirection.name, ownerUserId = owner
                    )
                )
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo el titular puede cancelar; la segunda cancelación no devuelve asientos. */
    suspend fun cancel(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            val booking = dao.get(id) ?: return@withTransaction
            if (!visible(booking, userId)) return@withTransaction
            if (dao.cancelIfActive(id) == 1) routeDao.restoreSeats(booking.routeId, booking.seats)
        }
    }

    suspend fun confirmPending(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            val booking = dao.get(id)?.takeIf { visible(it, userId) &&
                it.status == BookingStatus.PENDING.name } ?: return@withTransaction
            val route = routeDao.get(booking.routeId) ?: return@withTransaction
            if (route.status !in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name))
                return@withTransaction
            if (session.current.user?.id != userId) return@withTransaction
            dao.update(booking.copy(status = BookingStatus.CONFIRMED.name))
        }
    }

    suspend fun startTrip(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            val booking = dao.get(id)?.takeIf { visible(it, userId) &&
                it.status == BookingStatus.CONFIRMED.name && it.progress < 1 }
                ?: return@withTransaction
            val route = routeDao.get(booking.routeId) ?: return@withTransaction
            if (route.status !in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name))
                return@withTransaction
            if (session.current.user?.id != userId) return@withTransaction
            dao.update(booking.copy(progress = 1))
        }
    }

    suspend fun advance(id: Long, stageCount: Int) {
        val userId = session.current.user?.id ?: return
        if (stageCount <= 0) return
        db.withTransaction {
            val booking = dao.get(id)?.takeIf { visible(it, userId) &&
                it.status == BookingStatus.CONFIRMED.name && it.progress > 0 }
                ?: return@withTransaction
            val route = routeDao.get(booking.routeId) ?: return@withTransaction
            if (route.status !in listOf(RouteStatus.ACTIVE.name, RouteStatus.IN_PROGRESS.name))
                return@withTransaction
            if (session.current.user?.id != userId) return@withTransaction
            val next = (booking.progress + 1).coerceAtMost(stageCount)
            val status = if (next >= stageCount) BookingStatus.FINISHED.name else booking.status
            dao.update(booking.copy(progress = next, status = status))
        }
    }

    suspend fun rate(id: Long) {
        val userId = session.current.user?.id ?: return
        db.withTransaction {
            dao.get(id)?.takeIf { visible(it, userId) && it.status == BookingStatus.FINISHED.name && !it.rated }
                ?.let { dao.update(it.copy(rated = true)) }
        }
    }
}
