package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendAuthInterceptor
import mx.edu.uttt.rutitas.network.BackendUnauthorizedInterceptor
import mx.edu.uttt.rutitas.network.BackendUserDto
import mx.edu.uttt.rutitas.repository.BackendAuthRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackendSessionFailureTest {

    private lateinit var store: SecureTokenStore
    private lateinit var session: BackendSessionState

    private val testUser = BackendUserDto(
        id = "usuario-A",
        email = "alumno@uttt.edu.mx",
        displayName = "Alumno de prueba",
        phone = null,
        role = "STUDENT",
        createdAt = "2026-11-16T12:00:00Z"
    )

    @Before
    fun setup() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        store = SecureTokenStore(context)
        session = BackendSessionState()
        store.clear()
    }

    @After
    fun cleanup() {
        store.clear()
        session.clear()
    }

    private fun repository(
        status: Int? = null,
        offline: Boolean = false
    ): BackendAuthRepository {

        val client = OkHttpClient.Builder()
            .addInterceptor(BackendAuthInterceptor(store))
            .addInterceptor(
                BackendUnauthorizedInterceptor(store, session)
            )
            .addInterceptor(Interceptor { chain ->
                if (offline) {
                    throw IOException("Sin Internet - prueba")
                }

                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(status ?: 200)
                    .message("Respuesta simulada")
                    .body("{}".toResponseBody())
                    .build()
            })
            .build()

        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)

        return BackendAuthRepository(api, store, session)
    }

    @Test
    fun logout503ClearsLocalSession() = runBlocking {
        store.save("token-A", 3600L)
        session.setUser(testUser)

        var received503 = false

        try {
            repository(status = 503).logout()
        } catch (e: HttpException) {
            received503 = e.code() == 503
        }

        assertTrue("Se esperaba HTTP 503", received503)
        assertNull(store.read())
        assertNull(session.user.value)
    }

    @Test
    fun offlineRestorePreservesJwt() = runBlocking {
        store.save("token-A", 3600L)

        val restored = repository(offline = true)
            .restoreSession()

        assertFalse(restored)
        assertEquals("token-A", store.read())
    }
}
