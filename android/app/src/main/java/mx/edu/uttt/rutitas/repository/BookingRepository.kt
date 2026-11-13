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
    val bookings: Flow<List<Booking>> = dao.observeAll().map { l -> l.map { it.toDomain() } }

    /** Reservas activas cuya ruta fue interrumpida (notificación simulada al pasajero). */
    val interrupted: Flow<List<Booking>> = combine(bookings, routeDao.observeAll()) { bs, rs ->
        val broken = rs.filter { it.status == RouteStatus.INTERRUPTED.name }.map { it.id }.toSet()
        bs.filter { it.routeId in broken && (it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.PENDING) }
    }

    fun observe(id: Long): Flow<Booking?> = dao.observe(id).map { it?.toDomain() }

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
                val route = routeDao.get(routeId) ?: return@withTransaction fail("No encontramos la ruta.")
                if (route.status != RouteStatus.ACTIVE.name)
                    return@withTransaction fail("Esta ruta no admite reservas nuevas.")
                if (seats > route.seats)
                    return@withTransaction fail("Solo quedan ${route.seats} lugares.")
                if (routeDao.decrementSeatsIfAvailable(route.id, seats) != 1)
                    return@withTransaction fail("No hay suficientes asientos disponibles.")
                val id = dao.insert(
                    BookingEntity(
                        code = "RT-${UUID.randomUUID()}", routeId = route.id,
                        origin = route.origin, destination = route.destination,
                        departure = route.departure, pickup = pickup, driverName = route.driverName,
                        car = route.car, seats = seats, total = route.price * seats,
                        status = BookingStatus.CONFIRMED.name, progress = 0, rated = false,
                        seeded = false, dateLabel = "Hoy", shift = shift.name,
                        direction = direction.name, ownerUserId = owner
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
            if (booking.ownerUserId != userId) return@withTransaction
            if (dao.cancelIfActive(id) == 1) routeDao.restoreSeats(booking.routeId, booking.seats)
        }
    }

    suspend fun confirmPending(id: Long) {
        dao.get(id)?.let { dao.update(it.copy(status = BookingStatus.CONFIRMED.name)) }
    }

    suspend fun startTrip(id: Long) {
        dao.get(id)?.let { if (it.progress < 1) dao.update(it.copy(progress = 1)) }
    }

    suspend fun advance(id: Long, stageCount: Int) {
        val b = dao.get(id) ?: return
        val p = (b.progress + 1).coerceAtMost(stageCount)
        val status = if (p >= stageCount) BookingStatus.FINISHED.name else b.status
        dao.update(b.copy(progress = p, status = status))
    }

    suspend fun rate(id: Long) {
        dao.get(id)?.let { dao.update(it.copy(rated = true, status = BookingStatus.FINISHED.name)) }
    }
}
