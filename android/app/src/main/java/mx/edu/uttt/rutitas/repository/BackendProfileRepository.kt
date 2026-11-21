package mx.edu.uttt.rutitas.repository

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.data.SecureTokenStore
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendUserDto
import javax.inject.Inject
import javax.inject.Singleton

enum class PhoneAction {
    KEEP,
    UPDATE,
    REMOVE
}

@Singleton
class BackendProfileRepository @Inject constructor(
    private val api: BackendApiService,
    private val tokenStore: SecureTokenStore,
    private val session: BackendSessionState
) {
    suspend fun getProfile(): BackendUserDto {
        val token = synchronized(tokenStore) {
            tokenStore.read()
        } ?: error("No existe una sesion activa.")

        val profile = api.getMyProfile()

        synchronized(tokenStore) {
            check(tokenStore.read() == token) {
                "La sesion cambio durante la consulta."
            }
            session.setUser(profile)
        }

        return profile
    }

    suspend fun updateProfile(
        displayName: String?,
        phoneAction: PhoneAction,
        phone: String? = null
    ): BackendUserDto {
        val request = JsonObject()

        if (displayName != null) {
            require(displayName.isNotBlank()) {
                "El nombre no puede estar vacio."
            }
            request.addProperty("displayName", displayName.trim())
        }

        when (phoneAction) {
            PhoneAction.KEEP -> Unit

            PhoneAction.UPDATE -> {
                require(!phone.isNullOrBlank()) {
                    "El telefono es obligatorio."
                }
                request.addProperty("phone", phone.trim())
            }

            PhoneAction.REMOVE -> {
                request.add("phone", JsonNull.INSTANCE)
            }
        }

        require(request.size() > 0) {
            "No hay cambios para guardar."
        }

        val token = synchronized(tokenStore) {
            tokenStore.read()
        } ?: error("No existe una sesion activa.")

        val updated = api.updateMyProfileJson(request)

        synchronized(tokenStore) {
            check(tokenStore.read() == token) {
                "La sesion cambio durante la actualizacion."
            }
            session.setUser(updated)
        }

        return updated
    }
}
