package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendAuthInterceptor
import mx.edu.uttt.rutitas.network.BackendUnauthorizedInterceptor
import mx.edu.uttt.rutitas.repository.BackendAuthRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BackendConcurrentLoginTest {

    @Test
    fun olderLoginCannotReplaceNewerAccount() = runBlocking {
        val context =
            ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        val session = BackendSessionState()

        store.clear()

        val requestAStarted = CountDownLatch(1)
        val releaseA = CountDownLatch(1)

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(BackendAuthInterceptor(store))
                .addInterceptor(
                    BackendUnauthorizedInterceptor(store, session)
                )
                .addInterceptor(Interceptor { chain ->
                    val buffer = Buffer()
                    chain.request().body?.writeTo(buffer)
                    val requestBody = buffer.readUtf8()

                    val isA = requestBody.contains(
                        "alumno-a@uttt.edu.mx"
                    )

                    if (isA) {
                        requestAStarted.countDown()

                        if (!releaseA.await(15, TimeUnit.SECONDS)) {
                            throw IOException("Tiempo agotado para A")
                        }
                    }

                    val id = if (isA) "A" else "B"

                    val json = """
                        {
                          "accessToken": "token-$id",
                          "tokenType": "Bearer",
                          "expiresIn": 3600,
                          "user": {
                            "id": "$id",
                            "email": "alumno-$id@uttt.edu.mx",
                            "displayName": "Alumno $id",
                            "phone": null,
                            "role": "STUDENT",
                            "createdAt": "2026-11-16T12:00:00Z"
                          }
                        }
                    """.trimIndent()

                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
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

            val repo = BackendAuthRepository(
                api, store, session
            )

            val loginA = async(Dispatchers.IO) {
                runCatching {
                    repo.login("alumno-a@uttt.edu.mx", "password-A")
                }
            }

            try {
                assertTrue(
                    "La solicitud A no inicio",
                    requestAStarted.await(15, TimeUnit.SECONDS)
                )

                val userB = repo.login(
                    "alumno-b@uttt.edu.mx",
                    "password-B"
                )

                assertEquals("B", userB.id)
                assertEquals("token-B", store.read())
                assertEquals("B", session.user.value?.id)

                releaseA.countDown()

                val resultA = loginA.await()

                assertTrue(resultA.isFailure)
                assertTrue(
                    resultA.exceptionOrNull() is IllegalStateException
                )

                assertEquals("token-B", store.read())
                assertEquals("B", session.user.value?.id)
            } finally {
                releaseA.countDown()
                loginA.cancel()
            }
        } finally {
            store.clear()
            session.clear()
        }
    }
}