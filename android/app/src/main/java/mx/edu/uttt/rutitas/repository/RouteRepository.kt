package mx.edu.uttt.rutitas.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.RouteDao
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
    private val session: SessionManager
) {
    private val all: Flow<List<Route>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Rutas visibles al pasajero, ordenadas según su horario y zona. */
    val publicRoutes: Flow<List<Route>> = combine(all, session.state) { routes, s ->
        Recommender.rank(
            routes.filter { !it.ownedByMe && !it.alternative && it.status != RouteStatus.CANCELLED && it.status != RouteStatus.FINISHED },
            s
        )
    }

    val myRoutes: Flow<List<Route>> = all.map { list -> list.filter { it.ownedByMe } }

    fun observe(id: Long): Flow<Route?> = dao.observe(id).map { it?.toDomain() }

    fun alternatives(excludeId: Long): Flow<List<Route>> = all.map { list ->
        list.filter { !it.ownedByMe && it.id != excludeId && it.status == RouteStatus.ACTIVE && it.seats > 0 }
            .sortedByDescending { it.alternative }
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

    /** Edita una ruta que aún no ha iniciado. */
    suspend fun update(
        id: Long, origin: String, destination: String, departure: String, seats: Int, price: Int,
        days: String, recurring: Boolean, waitMinutes: Int, stops: List<String>? = null
    ): Result<Unit> {
        if (session.current.isDisabled) return Result.failure(IllegalStateException("Tu cuenta está inhabilitada. No puedes editar rutas."))
        val r = dao.get(id) ?: return Result.failure(IllegalStateException("No encontramos la ruta."))
        if (r.status != RouteStatus.ACTIVE.name && r.status != RouteStatus.INTERRUPTED.name) {
            return Result.failure(IllegalStateException("Solo puedes editar rutas que no han iniciado."))
        }
        val dep = TimeUtils.toMinutes(departure) ?: return Result.failure(IllegalArgumentException("Hora inválida"))
        delay(300)
        val newStops = (stops ?: if (origin == r.origin && destination == r.destination) r.stops.split("|") else defaultStops(origin, destination))
            .joinToString("|")
        dao.update(
            r.copy(
                origin = origin, destination = destination, departure = TimeUtils.format(dep),
                arrival = TimeUtils.format(dep + 30), seats = seats, price = price, days = days,
                recurring = recurring, waitMinutes = waitMinutes, stops = newStops
            )
        )
        return Result.success(Unit)
    }

    private fun defaultStops(origin: String, destination: String): List<String> =
        listOf(if (origin.endsWith("Centro")) origin else "$origin Centro", destination)

    suspend fun setStatus(id: Long, status: RouteStatus) {
        dao.get(id)?.let { dao.update(it.copy(status = status.name)) }
    }
}
