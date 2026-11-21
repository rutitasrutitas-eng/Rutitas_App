package mx.edu.uttt.rutitas.network

import mx.edu.uttt.rutitas.data.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Agrega JWT solamente a peticiones protegidas
 * del Backend real de Rutitas.
 *
 * Debe instalarse exclusivamente en el cliente
 * HTTP de BackendApiService.
 */
class BackendAuthInterceptor @Inject constructor(
    private val tokenStore: SecureTokenStore
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        val method = original.method

        val publicEndpoint =
            method == "POST" &&
            (path.endsWith("/auth/login") ||
             path.endsWith("/auth/register"))

        if (publicEndpoint) {
            return chain.proceed(original)
        }

        if (method == "POST" &&
            path.endsWith("/auth/logout") &&
            !original.header("Authorization").isNullOrBlank()
        ) {
            return chain.proceed(original)
        }

        val token = tokenStore.read()

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticated)
    }
}
