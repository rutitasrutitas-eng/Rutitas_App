package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendAuthInterceptor
import mx.edu.uttt.rutitas.network.BackendUnauthorizedInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendUnauthorizedInterceptorTest {

    private fun simulate(
        status: Int,
        replaceToken: Boolean = false,
        verifySession: Boolean = false
    ): String? {
        val context =
            ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        val session = mx.edu.uttt.rutitas.data.BackendSessionState()

        fun user(id: String) = mx.edu.uttt.rutitas.network.BackendUserDto(
            id = id,
            email = "$id@uttt.edu.mx",
            displayName = "Usuario $id",
            phone = null,
            role = "STUDENT",
            createdAt = "2026-11-16T12:00:00Z"
        )

        if (verifySession) session.setUser(user("A"))

        try {
            store.save("token-usuario-A", 3600L)

            val client = OkHttpClient.Builder()
                .addInterceptor(BackendAuthInterceptor(store))
                .addInterceptor(BackendUnauthorizedInterceptor(store, session))
                .addInterceptor(Interceptor { chain ->
                    if (replaceToken) {
                        store.save("token-usuario-B", 3600L)
                        if (verifySession) session.setUser(user("B"))
                    }

                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(status)
                        .message("Respuesta simulada")
                        .body("{}".toResponseBody())
                        .build()
                })
                .build()

            val request = Request.Builder()
                .url("https://example.invalid/api/v1/users/me")
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(status, response.code)
            }

            if (verifySession) {
                if (replaceToken) {
                    assertEquals("B", session.user.value?.id)
                } else if (status == 401) {
                    assertNull(session.user.value)
                }
            }

            return store.read()
        } finally {
            store.clear()
        }
    }

    @Test
    fun unauthorizedClearsCurrentToken() {
        assertNull(simulate(401))
    }

    @Test
    fun oldUnauthorizedDoesNotClearNewSession() {
        assertEquals(
            "token-usuario-B",
            simulate(401, replaceToken = true)
        )
    }

    @Test
    fun serverErrorDoesNotClearToken() {
        assertEquals(
            "token-usuario-A",
            simulate(500)
        )
    }

    @Test
    fun unauthorizedClearsUserIdentity() {
        assertNull(simulate(401, verifySession = true))
    }

    @Test
    fun oldUnauthorizedPreservesNewUserIdentity() {
        assertEquals(
            "token-usuario-B",
            simulate(401, replaceToken = true, verifySession = true)
        )
    }
}
