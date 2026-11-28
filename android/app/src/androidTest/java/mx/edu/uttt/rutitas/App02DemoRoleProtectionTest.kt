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
import mx.edu.uttt.rutitas.data.*
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.repository.AuthRepository
import mx.edu.uttt.rutitas.repository.DriverRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class App02DemoRoleProtectionTest {

    @Test
    fun demoRoleCannotChangeButProfileCanBeSaved() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tag = UUID.randomUUID().toString()
        val isolatedFiles = File(context.cacheDir, "role-test-$tag")
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
                        "rutitas_role_test_${tag}_$key",
                        mode
                    )
                }
            }
        }

        val db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
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

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            val original = session.current.user!!
            assertEquals(UserType.ALUMNO, original.type)

            // Intentos de cambiar el rol institucional.
            val unauthorizedTypes = UserType.entries.filter {
                it != UserType.ALUMNO
            }

            assertTrue(unauthorizedTypes.isNotEmpty())

            for (attemptedType in unauthorizedTypes) {
                val rejected = auth.updateProfile(
                    "Nombre No Autorizado",
                    original.email,
                    attemptedType,
                    "Coyote",
                    Shift.MATUTINO,
                    "8:00 AM",
                    "3:00 PM"
                )

                assertTrue(
                    "Se acepto el tipo $attemptedType",
                    rejected.isFailure
                )

                assertEquals(
                    UserType.ALUMNO,
                    session.current.user?.type
                )

                assertEquals(
                    original.name,
                    session.current.user?.name
                )
            }

            // Una modificacion legitima debe funcionar.
            val allowed = auth.updateProfile(
                "Perfil Alumno Validado",
                original.email,
                UserType.ALUMNO,
                "El Carmen",
                Shift.MATUTINO,
                "8:00 AM",
                "3:00 PM"
            )

            assertTrue(
                "El perfil valido no pudo guardarse",
                allowed.isSuccess
            )

            assertEquals(
                "Perfil Alumno Validado",
                session.current.user?.name
            )

            assertEquals(UserType.ALUMNO, session.current.user?.type)

            // Comprobar persistencia despues de cerrar sesion.
            auth.logout()

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isSuccess
            )

            assertEquals(
                "Perfil Alumno Validado",
                session.current.user?.name
            )

            assertEquals(UserType.ALUMNO, session.current.user?.type)
            assertEquals("El Carmen", session.current.zone)
            assertEquals("8:00 AM", session.current.entryTime)

        } finally {
            db.close()
            usedPrefs.values.forEach {
                it.edit().clear().commit()
            }
            isolatedFiles.deleteRecursively()
        }
    }
}