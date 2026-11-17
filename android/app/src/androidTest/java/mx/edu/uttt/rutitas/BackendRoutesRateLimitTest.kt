package mx.edu.uttt.rutitas

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.repository.BackendRoutesRepository
import mx.edu.uttt.rutitas.repository.RoutesRateLimitedException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class BackendRoutesRateLimitTest {

    private fun repository(
        retryAfter: String?,
        calls: AtomicInteger
    ): BackendRoutesRepository {

        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                calls.incrementAndGet()

                val response = Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(429)
                    .message("Too Many Requests")
                    .body(
                        """{"title":"Too Many Requests","status":429}"""
                            .toResponseBody()
                    )

                if (retryAfter != null) {
                    response.header("Retry-After", retryAfter)
                }

                response.build()
            })
            .build()

        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)

        return BackendRoutesRepository(api)
    }

    @Test
    fun retryAfterBlocksSecondRequest() = runBlocking {
        val calls = AtomicInteger(0)
        val repo = repository("60", calls)

        try {
            repo.getRoutes()
            fail("Se esperaba HTTP 429")
        } catch (e: HttpException) {
            assertEquals(429, e.code())
        }

        assertEquals(1, calls.get())

        try {
            repo.getRouteById("ruta-prueba")
            fail("Se esperaba bloqueo local")
        } catch (e: RoutesRateLimitedException) {
            assertTrue(e.secondsRemaining in 1L..60L)
        }

        assertEquals(
            "No debe enviarse otra peticion durante el bloqueo",
            1,
            calls.get()
        )
    }

    @Test
    fun missingRetryAfterDoesNotInventDelay() = runBlocking {
        val calls = AtomicInteger(0)
        val repo = repository(null, calls)

        repeat(2) {
            try {
                repo.getRoutes()
                fail("Se esperaba HTTP 429")
            } catch (e: HttpException) {
                assertEquals(429, e.code())
            }
        }

        assertEquals(
            "Sin Retry-After no debe inventarse un bloqueo",
            2,
            calls.get()
        )
    }
}
