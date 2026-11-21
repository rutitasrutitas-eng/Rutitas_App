package mx.edu.uttt.rutitas.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Almacena el JWT cifrado mediante una clave protegida por Android Keystore.
 * Exclusivo para la autenticaciÃ³n real del Backend.
 */
@Singleton
class SecureTokenStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(
        "rutitas_secure_auth",
        Context.MODE_PRIVATE
    )

    private val alias = "rutitas_backend_token_key"

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .build()
        )

        return generator.generateKey()
    }

    // Compatibilidad con las pruebas actuales.
    @Synchronized
    fun save(token: String) {
        save(token, 3600L)
    }

    // Para el login real: usar expiresIn del Backend.
    @Synchronized
    fun save(token: String, expiresInSeconds: Long) {
        require(token.isNotBlank())
        require(expiresInSeconds > 0)

        val now = System.currentTimeMillis()
        require(expiresInSeconds <= (Long.MAX_VALUE - now) / 1000L)
        val expiresAt = now + expiresInSeconds * 1000L

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())

        val encrypted = cipher.doFinal(
            token.toByteArray(Charsets.UTF_8)
        )

        prefs.edit()
            .putString(
                "token",
                Base64.encodeToString(
                    encrypted,
                    Base64.NO_WRAP
                )
            )
            .putString(
                "iv",
                Base64.encodeToString(
                    cipher.iv,
                    Base64.NO_WRAP
                )
            )
            .putLong("expires_at", expiresAt)
            .commit()
    }

    @Synchronized
    fun read(): String? {
        val expiresAt = prefs.getLong("expires_at", 0L)

        if (expiresAt <= System.currentTimeMillis()) {
            clear()
            return null
        }

        val encrypted = prefs.getString("token", null)
            ?: return null
        val iv = prefs.getString("iv", null)
            ?: return null

        return try {
            val cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
            )

            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(
                    128,
                    Base64.decode(iv, Base64.NO_WRAP)
                )
            )

            String(
                cipher.doFinal(
                    Base64.decode(
                        encrypted,
                        Base64.NO_WRAP
                    )
                ),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            clear()
            null
        }
    }

    /**
     * Evita que una respuesta HTTP 401 antigua
     * elimine el token de una sesión más reciente.
     */
    @Synchronized
    fun clearIfMatches(expectedToken: String): Boolean {
        if (expectedToken.isBlank()) return false

        val currentToken = read() ?: return false

        if (currentToken != expectedToken) {
            return false
        }

        clear()
        return true
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().commit()
    }
}
