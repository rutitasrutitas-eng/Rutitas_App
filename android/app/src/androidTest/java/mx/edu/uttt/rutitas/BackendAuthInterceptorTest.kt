package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendAuthInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendAuthInterceptorTest {

    private fun capture(
        path: String,
        method: String = "GET",
        token: String? = "jwt-ficticio"
    ): String? {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        try {
            if (token != null) store.save(token)

            var captured: String? = null

            val client = OkHttpClient.Builder()
                .addInterceptor(BackendAuthInterceptor(store))
                .addInterceptor(Interceptor { chain ->
                    captured = chain.request().header("Authorization")
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("{}".toResponseBody())
                        .build()
                })
                .build()

            val request = Request.Builder()
                .url("https://example.invalid/api/v1/$path")
                .method(method, if (method == "POST") {
                    "{}".toRequestBody(
                        "application/json".toMediaType()
                    )
                } else null)
                .build()

            client.newCall(request).execute().use { }
            return captured
        } finally {
            store.clear()
        }
    }

    @Test
    fun loginDoesNotSendToken() {
        assertNull(capture("auth/login", "POST"))
    }

    @Test
    fun registerDoesNotSendToken() {
        assertNull(capture("auth/register", "POST"))
    }

    @Test
    fun protectedRequestSendsBearer() {
        assertEquals(
            "Bearer jwt-ficticio",
            capture("users/me")
        )
    }

    @Test
    fun missingTokenDoesNotSendBearer() {
        assertNull(capture("routes", token = null))
    }
}
