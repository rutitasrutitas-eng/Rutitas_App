package mx.edu.uttt.rutitas.domain

import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.utils.TimeUtils
import kotlin.math.abs

/** Ordena las rutas según la hora de entrada y la zona del usuario y asigna las etiquetas amarillas. */
object Recommender {
    private data class Scored(val route: Route, val zone: Boolean, val onTime: Boolean, val diff: Int)

    fun rank(routes: List<Route>, session: Session): List<Route> {
        val entry = TimeUtils.toMinutes(session.entryTime) ?: 420
        val target = entry - 15
        val scored = routes.map { r ->
            val arr = TimeUtils.toMinutes(r.arrival) ?: 0
            Scored(
                r,
                zone = session.zone != null && r.origin.equals(session.zone, true),
                onTime = arr in (entry - 90)..(entry - 5),
                diff = abs(arr - target)
            )
        }.sortedWith(compareBy<Scored>({ !it.zone }, { !it.onTime }, { it.diff }))

        val onTimeId = scored.firstOrNull { it.onTime }?.route?.id
        val recommendedId = scored.firstOrNull { it.route.id != onTimeId }?.route?.id
        return scored.map { s ->
            val tag = s.route.tag ?: when (s.route.id) {
                onTimeId -> "Llega a tiempo"
                recommendedId -> "Recomendada"
                else -> null
            }
            s.route.copy(tag = tag)
        }
    }
}
