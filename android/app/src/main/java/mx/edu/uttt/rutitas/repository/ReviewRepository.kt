package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.ReviewDao
import mx.edu.uttt.rutitas.database.ReviewEntity
import mx.edu.uttt.rutitas.database.toDomain
import mx.edu.uttt.rutitas.model.Review
import mx.edu.uttt.rutitas.model.ReviewRole
import javax.inject.Inject
import javax.inject.Singleton

/** Reseñas entre conductores y pasajeros (en ambos sentidos). */
@Singleton
class ReviewRepository @Inject constructor(private val dao: ReviewDao, private val session: SessionManager) {
    val reviews: Flow<List<Review>> = dao.observeAll().map { l -> l.map { it.toDomain() } }

    /** Devuelve false si ya existía una reseña con esa referencia (evita calificar dos veces el mismo viaje). */
    suspend fun add(
        refKey: String, role: ReviewRole, from: String, to: String, stars: Int,
        tags: Set<String>, comment: String, routeTitle: String
    ): Boolean {
        if (dao.findByRef(refKey) != null) return false
        dao.insert(
            ReviewEntity(
                refKey = refKey, role = role.name, fromName = from, toName = to, stars = stars.coerceIn(1, 5),
                tags = tags.joinToString("|"), comment = comment.trim(), routeTitle = routeTitle, dateLabel = "Hoy",
                ownerUserId = session.current.user?.id
            )
        )
        return true
    }
}
