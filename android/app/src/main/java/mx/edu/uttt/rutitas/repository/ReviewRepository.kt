package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.RequestDao
import mx.edu.uttt.rutitas.database.ReviewDao
import mx.edu.uttt.rutitas.database.ReviewEntity
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.database.toDomain
import mx.edu.uttt.rutitas.model.Review
import mx.edu.uttt.rutitas.model.ReviewRole
import mx.edu.uttt.rutitas.model.BookingStatus
import mx.edu.uttt.rutitas.model.RequestStatus
import javax.inject.Inject
import javax.inject.Singleton

/** Reseñas entre conductores y pasajeros (en ambos sentidos). */
@Singleton
class ReviewRepository @Inject constructor(
    private val dao: ReviewDao,
    private val session: SessionManager,
    private val bookingDao: BookingDao,
    private val routeDao: RouteDao,
    private val requestDao: RequestDao,
    private val db: AppDatabase
) {
    val reviews: Flow<List<Review>> = combine(dao.observeAll(), session.state) { rows, s ->
        rows.filter { it.ownerUserId == s.user?.id && s.user != null ||
            (it.ownerUserId == null && it.refKey.startsWith("seed")) }
            .map { it.toDomain() }
    }

    /** Las calificaciones de conductores son públicas; las demás reseñas siguen la sesión. */
    val publicDriverReviews: Flow<List<Review>> = dao.observeAll().map { rows ->
        rows.filter { it.role == ReviewRole.PASSENGER_TO_DRIVER.name &&
            (it.ownerUserId != null || it.refKey.startsWith("seed")) }
            .map { it.toDomain() }
    }

    /** Devuelve false si ya existía una reseña con esa referencia (evita calificar dos veces el mismo viaje). */
    suspend fun add(
        refKey: String, role: ReviewRole, from: String, to: String, stars: Int,
        tags: Set<String>, comment: String, routeTitle: String
    ): Boolean {
        val user = session.current.user ?: return false
        if (session.current.isDisabled) return false
        return db.withTransaction {
            val parts = refKey.split(":")
            val permitted = when {
                parts.size == 2 && parts[0] == "booking" && role == ReviewRole.PASSENGER_TO_DRIVER -> {
                    val b = parts[1].toLongOrNull()?.let { bookingDao.get(it) }
                    b != null && (b.ownerUserId == user.id || (user.id == 0L && b.seeded && b.ownerUserId == null)) &&
                        b.status == BookingStatus.FINISHED.name && !b.rated && to == b.driverName
                }
                parts.size == 4 && parts[0] == "route" && parts[2] == "req" && role == ReviewRole.DRIVER_TO_PASSENGER -> {
                    val r = parts[1].toLongOrNull()?.let { routeDao.get(it) }
                    val q = parts[3].toLongOrNull()?.let { requestDao.get(it) }
                    r != null && q != null && r.ownerUserId == user.id && q.ownerUserId == user.id && q.routeId == r.id &&
                        q.status == RequestStatus.ACCEPTED.name && to == q.passengerName
                }
                else -> false
            }
            if (!permitted || dao.findByRef(refKey) != null) return@withTransaction false
            dao.insert(ReviewEntity(
                refKey = refKey, role = role.name, fromName = user.name, toName = to, stars = stars.coerceIn(1, 5),
                tags = tags.joinToString("|"), comment = comment.trim(), routeTitle = routeTitle, dateLabel = "Hoy",
                ownerUserId = user.id
            )
            )
            true
        }
    }
}
