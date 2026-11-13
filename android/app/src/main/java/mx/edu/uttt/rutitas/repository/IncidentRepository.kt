package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.RequestDao
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.model.Incident
import mx.edu.uttt.rutitas.model.IncidentType
import mx.edu.uttt.rutitas.model.RouteStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val bookingDao: BookingDao,
    private val requestDao: RequestDao,
    private val driverRepo: DriverRepository,
    private val session: SessionManager
) {
    val incidents = driverRepo.incidents

    /** Registra la incidencia. Si es avería: interrumpe la ruta, bloquea reservas y "notifica" pasajeros. */
    suspend fun report(routeId: Long, type: IncidentType): Int {
        val userId = session.current.user?.id ?: return 0
        val route = routeDao.get(routeId)?.takeIf { it.ownerUserId == userId } ?: return 0
        var notified = 0
        if (type == IncidentType.BREAKDOWN) {
            routeDao.update(route.copy(status = RouteStatus.INTERRUPTED.name))
            notified = bookingDao.activeForRoute(routeId)
        }
        driverRepo.addIncident(
            Incident(System.nanoTime(), route?.let { "${it.origin} → ${it.destination}" } ?: "Ruta", type, notified, "Ahora")
        )
        return notified
    }
}
