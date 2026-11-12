package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.delay
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
    private val session: SessionManager
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
        if (s.user == null) return fail("Inicia sesión para reservar.")
        if (s.isDisabled) return fail("Tu cuenta está inhabilitada. No puedes reservar.")
        val r = routeDao.get(routeId) ?: return fail("No encontramos la ruta.")
        if (r.status == RouteStatus.INTERRUPTED.name) return fail("Esta ruta está interrumpida y no acepta reservas.")
        if (r.seats < seats) return fail(if (r.seats == 0) "La ruta ya no tiene lugares." else "Solo quedan ${r.seats} lugares.")
        delay(450)
        routeDao.update(r.copy(seats = r.seats - seats))
        val n = dao.countCreated()
        val id = dao.insert(
            BookingEntity(
                code = "RT-${2481 + n}", routeId = r.id, origin = r.origin, destination = r.destination,
                departure = r.departure, pickup = pickup, driverName = r.driverName, car = r.car,
                seats = seats, total = r.price * seats, status = BookingStatus.CONFIRMED.name,
                progress = 0, rated = false, seeded = false, dateLabel = "Hoy",
                shift = shift.name, direction = direction.name
            )
        )
        return Result.success(id)
    }

    suspend fun cancel(id: Long) {
        val b = dao.get(id) ?: return
        routeDao.get(b.routeId)?.let { routeDao.update(it.copy(seats = it.seats + b.seats)) }
        dao.update(b.copy(status = BookingStatus.CANCELLED.name))
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
