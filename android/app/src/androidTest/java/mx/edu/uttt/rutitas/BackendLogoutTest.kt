package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackendLogoutTest {

    private lateinit var store: SecureTokenStore
    private lateinit var session: BackendSessionState

    private fun user(id: String) = BackendUserDto(
        id = id,
        email = "$id@uttt.edu.mx",
        displayName = "Usuario $id",
        phone = null,
        role = "STUDENT",
        createdAt = "2026-11-16T12:00:00Z"
    )

    @Before
    fun setup() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        store = SecureTokenStore(context)
        session = BackendSessionState()
        store.clear()

        store.save("token-A", 3600L)
        session.setUser(user("A"))
    }

    @After
    fun cleanup() {
        store.clear()
        session.clear()
    }

    private fun repository(
        failConnection: Boolean = false,
        switchAccount: Boolean = false,
        requestStarted: CountDownLatch? = null,
        releaseResponse: CountDownLatch? = null
    ): BackendAuthRepository {

        val client = OkHttpClient.Builder()
            .addInterceptor(BackendAuthInterceptor(store))
            .addInterceptor(
                BackendUnauthorizedInterceptor(store, session)
            )
            .addInterceptor(Interceptor { chain ->

                assertEquals(
                    "Bearer token-A",
                    chain.request().header("Authorization")
                )

                if (requestStarted != null && releaseResponse != null) {
                    requestStarted.countDown()
                    if (!releaseResponse.await(15, TimeUnit.SECONDS)) {
                        throw IOException("Tiempo de espera agotado")
                    }
                }

                if (switchAccount) {
                    synchronized(store) {
                        store.save("token-B", 3600L)
                        session.setUser(user("B"))
                    }

                    assertEquals(
                        "Bearer token-A",
                        chain.request().header("Authorization")
                    )
                }

                if (failConnection) {
                    throw IOException("Conexion simulada interrumpida")
                }

                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(204)
                    .message("No Content")
                    .body("".toResponseBody())
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
    fun successfulLogoutClearsLocalSession() = runBlocking {
        repository().logout()

        assertNull(store.read())
        assertNull(session.user.value)
    }

    @Test
    fun networkFailureStillClearsLocalSession() = runBlocking {
        var connectionFailed = false

        try {
            repository(failConnection = true).logout()
        } catch (e: IOException) {
            connectionFailed = true
        }

        assertTrue(connectionFailed)
        assertNull(store.read())
        assertNull(session.user.value)
    }

    @Test
    fun oldLogoutPreservesNewAccount() = runBlocking {
        repository(switchAccount = true).logout()

        assertEquals("token-B", store.read())
        assertEquals("B", session.user.value?.id)
    }

    @Test
    fun delayedLogoutDoesNotEraseNewAccount() = runBlocking {
        val requestStarted = CountDownLatch(1)
        val releaseResponse = CountDownLatch(1)

        val repo = repository(
            requestStarted = requestStarted,
            releaseResponse = releaseResponse
        )

        val logoutJob = async(Dispatchers.IO) {
            repo.logout()
        }

        try {
            assertTrue(
                "El logout de A no llego al interceptor",
                requestStarted.await(15, TimeUnit.SECONDS)
            )

            synchronized(store) {
                store.save("token-B", 3600L)
                session.setUser(user("B"))
            }

            releaseResponse.countDown()
            logoutJob.await()

            assertEquals("token-B", store.read())
            assertEquals("B", session.user.value?.id)
        } finally {
            releaseResponse.countDown()
        }
    }
}
