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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendSessionRestoreTest {

    private lateinit var store: SecureTokenStore
    private lateinit var session: BackendSessionState

    private val user = BackendUserDto(
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
        status: Int = 200,
        replaceToken: Boolean = false
    ): BackendAuthRepository {

        val json = """
            {
              "id": "usuario-A",
              "email": "alumno@uttt.edu.mx",
              "displayName": "Alumno de prueba",
              "phone": null,
              "role": "STUDENT",
              "createdAt": "2026-11-16T12:00:00Z"
            }
        """.trimIndent()

        val client = OkHttpClient.Builder()
            .addInterceptor(BackendAuthInterceptor(store))
            .addInterceptor(BackendUnauthorizedInterceptor(store, session))
            .addInterceptor(Interceptor { chain ->
                if (replaceToken) {
                    store.save("token-nuevo", 3600L)
                }

                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(status)
                    .message("Respuesta simulada")
                    .body(json.toResponseBody())
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
    fun validTokenRestoresProfile() = runBlocking {
        store.save("token-valido", 3600L)

        assertTrue(repository().restoreSession())
        assertEquals("usuario-A", session.user.value?.id)
        assertEquals("token-valido", store.read())
    }

    @Test
    fun missingTokenDoesNotRestore() = runBlocking {
        assertFalse(repository().restoreSession())
        assertNull(session.user.value)
    }

    @Test
    fun unauthorizedClearsOldSession() = runBlocking {
        store.save("token-invalido", 3600L)
        session.setUser(user)

        assertFalse(repository(status = 401).restoreSession())
        assertNull(store.read())
        assertNull(session.user.value)
    }

    @Test
    fun serverErrorPreservesToken() = runBlocking {
        store.save("token-valido", 3600L)

        assertFalse(repository(status = 500).restoreSession())
        assertEquals("token-valido", store.read())
    }

    @Test
    fun oldResponseDoesNotOverwriteNewSession() = runBlocking {
        store.save("token-anterior", 3600L)

        assertFalse(
            repository(
                status = 200,
                replaceToken = true
            ).restoreSession()
        )

        assertEquals("token-nuevo", store.read())
        assertNull(session.user.value)
    }
}
