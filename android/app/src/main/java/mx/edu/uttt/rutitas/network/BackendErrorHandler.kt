package mx.edu.uttt.rutitas.network

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

data class BackendErrorInfo(
    val statusCode: Int?,
    val message: String,
    val retryAfterSeconds: Long? = null,
    val requiresLogin: Boolean = false,
    val isNetworkError: Boolean = false
)

object BackendErrorHandler {

    fun from(exception: Exception): BackendErrorInfo {
        if (exception is mx.edu.uttt.rutitas.repository.RoutesRateLimitedException) {
            return BackendErrorInfo(
                statusCode = 429,
                message = exception.message
                    ?: "Demasiadas solicitudes.",
                retryAfterSeconds = exception.secondsRemaining
            )
        }
        if (exception is IOException) {
            return BackendErrorInfo(
                statusCode = null,
                message = "No se pudo conectar con el servidor. Revisa tu conexion.",
                isNetworkError = true
            )
        }

        if (exception !is HttpException) {
            return BackendErrorInfo(
                statusCode = null,
                message = "No se pudo completar la operacion."
            )
        }

        val status = exception.code()

        val retryAfter = exception.response()
            ?.headers()
            ?.get("Retry-After")
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it >= 0 }

        val validationMessage = if (status == 400) {
            try {
                val body = exception.response()
                    ?.errorBody()
                    ?.string()

                if (body.isNullOrBlank()) {
                    null
                } else {
                    val json = JsonParser.parseString(body).asJsonObject
                    val errors = json.getAsJsonObject("errors")

                    errors?.entrySet()?.firstOrNull()?.value
                        ?.asJsonArray
                        ?.firstOrNull()
                        ?.asString
                        ?.take(200)
                }
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        val message = when (status) {
            400 -> validationMessage
                ?: "Revisa los datos ingresados."
            401 -> "Tu sesion ya no es valida. Inicia sesion nuevamente."
            403 -> "No tienes permiso para esta operacion."
            404 -> "El recurso solicitado no existe."
            409 -> "Los datos entran en conflicto con un registro existente."
            429 -> if (retryAfter != null) {
                "Demasiadas solicitudes. Espera $retryAfter segundos antes de intentar nuevamente."
            } else {
                "Demasiadas solicitudes. Intenta mas tarde."
            }
            503 -> "El servidor no esta disponible temporalmente."
            in 500..599 -> "Ocurrio un problema en el servidor."
            else -> "No fue posible completar la operacion."
        }

        return BackendErrorInfo(
            statusCode = status,
            message = message,
            retryAfterSeconds = retryAfter,
            requiresLogin = status == 401
        )
    }
}
