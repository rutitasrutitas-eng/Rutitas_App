package mx.edu.uttt.rutitas.network

import mx.edu.uttt.rutitas.data.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Invalida el JWT cuando el Backend rechaza
 * una petición autenticada con HTTP 401.
 *
 * El aviso a la interfaz se conectará después.
 */
class BackendUnauthorizedInterceptor @Inject constructor(
    private val tokenStore: SecureTokenStore,
    private val backendSession: mx.edu.uttt.rutitas.data.BackendSessionState
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val bearer = request.header("Authorization")
            val prefix = "Bearer "

            if (bearer != null &&
                bearer.startsWith(prefix, ignoreCase = true)
            ) {
                val rejectedToken = bearer.substring(prefix.length)
                synchronized(tokenStore) {
                    if (tokenStore.clearIfMatches(rejectedToken)) {
                        backendSession.clear()
                    }
                }
            }
        }

        return response
    }
}
