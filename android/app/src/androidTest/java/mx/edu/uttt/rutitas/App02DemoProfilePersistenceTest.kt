package mx.edu.uttt.rutitas

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.DriverLocalStore
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.repository.AuthRepository
import mx.edu.uttt.rutitas.repository.DriverRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class App02DemoProfilePersistenceTest {

    @Test
    fun demoProfilePersistsAndIsClearedOnDeletionAndRestore() = runBlocking {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        val tag = UUID.randomUUID().toString()
        val isolatedFiles = File(
            context.cacheDir,
            "rutitas-profile-test-$tag"
        )

        assertTrue(isolatedFiles.mkdirs())

        val usedPrefs = mutableMapOf<String, SharedPreferences>()

        val isolatedContext = object : ContextWrapper(context) {
            override fun getFilesDir(): File = isolatedFiles

            override fun getSharedPreferences(
                name: String?,
                mode: Int
            ): SharedPreferences {
                val key = name ?: "default"
                return usedPrefs.getOrPut(key) {
                    context.getSharedPreferences(
                        "rutitas_profile_test_${tag}_$key",
                        mode
                    )
                }
            }
        }

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            val session = SessionManager()
            val photoStore = PrivatePhotoStore(isolatedContext)
            val driverStore = DriverLocalStore(isolatedContext)

            val driverRepo = DriverRepository(
                db.routeDao(),
                db.requestDao(),
                db.bookingDao(),
                session,
                driverStore,
                photoStore,
                db
            )

            val seeder = DemoSeeder(
                db.routeDao(),
                db.bookingDao(),
                db.requestDao(),
                db.reviewDao(),
                driverRepo,
                session
            )

            val auth = AuthRepository(
                db.userDao(),
                db.bookingDao(),
                db.routeDao(),
                db.requestDao(),
                db.reviewDao(),
                photoStore,
                driverStore,
                driverRepo,
                seeder,
                session,
                db,
                isolatedContext
            )

            val prefs = isolatedContext.getSharedPreferences(
                "rutitas_prefs",
                Context.MODE_PRIVATE
            )

            // 1. Entrar con cuenta DEMO principal.
            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                DemoData.demoUser.name,
                session.current.user?.name
            )

            // 2. Editar y guardar el perfil.
            val changedName = "Perfil Demo Persistente"

            val update = auth.updateProfile(
                changedName,
                DemoData.DEMO_EMAIL,
                UserType.ALUMNO,
                "El Carmen",
                Shift.MATUTINO,
                "8:00 AM",
                "3:00 PM"
            )

            assertTrue(
                "No se pudo guardar el perfil",
                update.isSuccess
            )

            assertEquals(
                changedName,
                session.current.user?.name
            )

            // 3. Simular una nueva sesion sin
            // restauracion automatica.
            auth.logout()

            assertNull(session.current.user)

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                changedName,
                session.current.user?.name
            )

            assertEquals(
                "El Carmen",
                session.current.zone
            )

            assertEquals(
                "8:00 AM",
                session.current.entryTime
            )

            // 4. Comprobar aislamiento entre cuentas.
            auth.logout()

            assertTrue(
                auth.login(
                    DemoData.BLOCKED_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                DemoData.blockedUser.name,
                session.current.user?.name
            )

            assertNotEquals(
                changedName,
                session.current.user?.name
            )

            // 5. La cuenta principal sigue intacta.
            auth.logout()

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                changedName,
                session.current.user?.name
            )

            // 6. Eliminar cuenta: no debe conservar
            // informacion editable eliminada.
            auth.deleteAccount()

            assertNull(session.current.user)

            assertFalse(
                prefs.contains("demo_profile_0_name")
            )

            assertFalse(
                prefs.contains("demo_profile_0_zone")
            )

            assertTrue(
                prefs.getBoolean(
                    "deleted_${DemoData.DEMO_EMAIL}",
                    false
                )
            )

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isFailure
            )

            // 7. Restaurar DEMO desde valores iniciales.
            auth.restoreDemoAccounts()

            assertFalse(
                prefs.contains("demo_profile_0_name")
            )

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                DemoData.demoUser.name,
                session.current.user?.name
            )

            assertNotEquals(
                changedName,
                session.current.user?.name
            )

            // 8. La otra cuenta no debe recuperar
            // los datos del perfil eliminado.
            auth.logout()

            assertTrue(
                auth.login(
                    DemoData.BLOCKED_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                DemoData.blockedUser.name,
                session.current.user?.name
            )

        } finally {
            db.close()
            usedPrefs.values.forEach {
                it.edit().clear().commit()
            }
            isolatedFiles.deleteRecursively()
        }
    }
}