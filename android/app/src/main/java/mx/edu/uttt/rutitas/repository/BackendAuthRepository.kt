package mx.edu.uttt.rutitas.repository

import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendUserDto
import mx.edu.uttt.rutitas.network.LoginRequestDto
import mx.edu.uttt.rutitas.network.RegisterRequestDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Autenticación real contra el Backend de Rutitas.
 * Independiente de las cuentas DEMO almacenadas en Room.
 */
@Singleton
class BackendAuthRepository @Inject constructor(
    private val api: BackendApiService,
    private val tokenStore: SecureTokenStore,
    private val backendSession: BackendSessionState
) {

    private var loginAttemptId = 0L

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        phone: String? = null
    ): BackendUserDto {
        return api.register(
            RegisterRequestDto(
                email = email,
                password = password,
                displayName = displayName,
                phone = phone
            )
        )
    }

    suspend fun login(
        email: String,
        password: String
    ): BackendUserDto {
        val attempt = synchronized(tokenStore) {
            ++loginAttemptId
        }

        val response = api.login(
            LoginRequestDto(email, password)
        )

        require(
            response.tokenType.equals("Bearer", ignoreCase = true)
        ) {
            "Tipo de token no admitido."
        }

        synchronized(tokenStore) {
            check(attempt == loginAttemptId) {
                "Este intento de inicio de sesion fue reemplazado por uno nuevo."
            }

            tokenStore.save(
                response.accessToken,
                response.expiresIn
            )
            backendSession.setUser(response.user)
        }

        return response.user
    }

    suspend fun logout() {
        val tokenAtStart = synchronized(tokenStore) {
            ++loginAttemptId
            tokenStore.read()
        }

        try {
            if (tokenAtStart != null) {
                api.logout("Bearer $tokenAtStart")
            }
        } finally {
            synchronized(tokenStore) {
                if (tokenAtStart != null) {
                    if (tokenStore.clearIfMatches(tokenAtStart)) {
                        backendSession.clear()
                    }
                } else if (tokenStore.read() == null) {
                    backendSession.clear()
                }
            }
        }
    }

    suspend fun restoreSession(): Boolean {
        val requestedToken = synchronized(tokenStore) {
            tokenStore.read()
        }

        if (requestedToken == null) {
            synchronized(tokenStore) {
                if (tokenStore.read() == null) {
                    backendSession.clear()
                }
            }
            return false
        }

        return try {
            val user = api.getMyProfile()

            synchronized(tokenStore) {
                if (tokenStore.read() != requestedToken) {
                    false
                } else {
                    backendSession.setUser(user)
                    true
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                synchronized(tokenStore) {
                    if (tokenStore.clearIfMatches(requestedToken)) {
                        backendSession.clear()
                    } else if (tokenStore.read() == null) {
                        backendSession.clear()
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun hasValidSession(): Boolean =
        tokenStore.read() != null
}
