package mx.edu.uttt.rutitas

import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.network.BackendErrorHandler
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackendErrorHandlerTest {

    private fun httpError(
        status: Int,
        retryAfter: String? = null,
        body: String = "{}"
    ): HttpException {
        val headers = Headers.Builder()

        if (retryAfter != null) {
            headers.add("Retry-After", retryAfter)
        }

        val rawResponse = okhttp3.Response.Builder()
            .request(
                Request.Builder()
                    .url("https://example.invalid/api/v1/routes")
                    .build()
            )
            .protocol(Protocol.HTTP_1_1)
            .code(status)
            .message("HTTP error")
            .headers(headers.build())
            .build()

        val response = Response.error<Any>(
            body.toResponseBody(
                "application/problem+json".toMediaType()
            ),
            rawResponse
        )

        return HttpException(response)
    }

    @Test
    fun unauthorizedRequiresLogin() {
        val result = BackendErrorHandler.from(httpError(401))

        assertEquals(401, result.statusCode)
        assertTrue(result.requiresLogin)
        assertFalse(result.isNetworkError)
    }

    @Test
    fun conflictDoesNotRequireLogin() {
        val result = BackendErrorHandler.from(httpError(409))

        assertEquals(409, result.statusCode)
        assertFalse(result.requiresLogin)
    }

    @Test
    fun rateLimitReadsRetryAfter() {
        val result = BackendErrorHandler.from(
            httpError(429, retryAfter = "60")
        )

        assertEquals(429, result.statusCode)
        assertEquals(60L, result.retryAfterSeconds)
        assertFalse(result.requiresLogin)
    }

    @Test
    fun serviceUnavailableDoesNotRequireLogin() {
        val result = BackendErrorHandler.from(httpError(503))

        assertEquals(503, result.statusCode)
        assertFalse(result.requiresLogin)
    }

    @Test
    fun networkErrorDoesNotInvalidateSession() {
        val result = BackendErrorHandler.from(
            IOException("Sin conexion")
        )

        assertNull(result.statusCode)
        assertTrue(result.isNetworkError)
        assertFalse(result.requiresLogin)
    }

    @Test
    fun validationErrorReadsFieldMessage() {
        val body = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "errors": {
                "displayName": ["Nombre obligatorio"]
              }
            }
        """.trimIndent()

        val result = BackendErrorHandler.from(
            httpError(400, body = body)
        )

        assertEquals(400, result.statusCode)
        assertEquals("Nombre obligatorio", result.message)
    }
}
