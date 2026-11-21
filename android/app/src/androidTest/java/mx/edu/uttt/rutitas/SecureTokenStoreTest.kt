package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.data.SecureTokenStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureTokenStoreTest {

    @Test
    fun tokenIsStoredEncryptedAndCanBeRecovered() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()
        val store = SecureTokenStore(context)

        store.clear()

        try {
            val fakeToken = "jwt-ficticio-para-prueba"
            store.save(fakeToken)

            val reopenedStore = SecureTokenStore(context)

            assertEquals(fakeToken, reopenedStore.read())

            val prefs = context.getSharedPreferences(
                "rutitas_secure_auth",
                Context.MODE_PRIVATE
            )

            assertNotEquals(
                fakeToken,
                prefs.getString("token", null)
            )
        } finally {
            store.clear()
        }
    }

    @Test
    fun tokenCanBeDeleted() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()
        val store = SecureTokenStore(context)

        store.clear()
        store.save("jwt-ficticio")
        store.clear()

        assertNull(store.read())
    }

    @Test
    fun validTokenCanBeRecovered() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        try {
            store.save("jwt-vigente", 3600L)
            assertEquals("jwt-vigente", store.read())
        } finally {
            store.clear()
        }
    }

    @Test
    fun expiredTokenIsRejected() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        try {
            store.save("jwt-vencido", 3600L)

            val prefs = context.getSharedPreferences(
                "rutitas_secure_auth",
                Context.MODE_PRIVATE
            )

            prefs.edit()
                .putLong("expires_at", System.currentTimeMillis() - 1000L)
                .commit()

            assertNull(store.read())
            assertNull(prefs.getString("token", null))
        } finally {
            store.clear()
        }
    }

    @Test
    fun currentTokenCanBeInvalidated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        try {
            store.save("token-actual", 3600L)

            assertTrue(store.clearIfMatches("token-actual"))
            assertNull(store.read())
        } finally {
            store.clear()
        }
    }

    @Test
    fun oldTokenCannotClearNewSession() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SecureTokenStore(context)
        store.clear()

        try {
            store.save("token-usuario-A", 3600L)
            store.save("token-usuario-B", 3600L)

            assertFalse(
                store.clearIfMatches("token-usuario-A")
            )

            assertEquals(
                "token-usuario-B",
                store.read()
            )
        } finally {
            store.clear()
        }
    }
}
