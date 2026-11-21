package mx.edu.uttt.rutitas.repository

import android.os.SystemClock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendErrorHandler
import mx.edu.uttt.rutitas.network.BackendRouteDetailDto
import mx.edu.uttt.rutitas.network.PagedRoutesDto
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

class RoutesRateLimitedException(
    val secondsRemaining: Long
) : Exception(
    "Demasiadas solicitudes. Espera $secondsRemaining segundos antes de consultar rutas."
)

@Singleton
class BackendRoutesRepository @Inject constructor(
    private val api: BackendApiService
) {
    private val mutex = Mutex()
    private var blockedUntilMs: Long = 0

    suspend fun getRoutes(
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null
    ): PagedRoutesDto = execute {
        api.getRoutes(
            page = page,
            pageSize = pageSize,
            query = query
        )
    }

    suspend fun getRouteById(id: String): BackendRouteDetailDto {
        require(id.isNotBlank()) {
            "El identificador de la ruta es obligatorio."
        }

        return execute {
            api.getRouteById(id)
        }
    }

    private suspend fun <T> execute(
        request: suspend () -> T
    ): T = mutex.withLock {
        val now = SystemClock.elapsedRealtime()
        val remainingMs = blockedUntilMs - now

        if (remainingMs > 0) {
            val remainingSeconds =
                remainingMs / 1000L +
                    if (remainingMs % 1000L > 0) 1L else 0L

            throw RoutesRateLimitedException(remainingSeconds)
        }

        try {
            request()
        } catch (e: HttpException) {
            if (e.code() == 429) {
                val seconds = BackendErrorHandler.from(e)
                    .retryAfterSeconds

                if (seconds != null && seconds > 0L) {
                    val current = SystemClock.elapsedRealtime()
                    val maximumSeconds =
                        (Long.MAX_VALUE - current) / 1000L

                    val safeSeconds =
                        seconds.coerceAtMost(maximumSeconds)

                    blockedUntilMs =
                        current + safeSeconds * 1000L
                }
            }

            throw e
        }
    }
}
