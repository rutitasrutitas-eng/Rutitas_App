package mx.edu.uttt.rutitas

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.DriverLocalStore
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.repository.AuthRepository
import mx.edu.uttt.rutitas.repository.DriverRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ProfilePhotoSessionRaceTest {

    @Test
    fun photoStartedByAIsNotAssignedToBAfterSessionSwitch() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

        val session = SessionManager()
        val photoStore = PrivatePhotoStore(context)
        val driverStore = DriverLocalStore(context)

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
            context
        )

        val userA = DemoData.demoUser.copy(id = 980001L)
        val userB = DemoData.blockedUser.copy(id = 980002L)

        val keyA = "profile_${userA.id}"
        val keyB = "profile_${userB.id}"

        val source = File.createTempFile(
            "app02-test-", ".jpg", context.cacheDir
        )

        try {
            photoStore.delete(keyA)
            photoStore.delete(keyB)

            val bitmap = Bitmap.createBitmap(
                32, 32, Bitmap.Config.ARGB_8888
            )

            FileOutputStream(source).use {
                assertTrue(
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                )
            }
            bitmap.recycle()

            session.update { Session(user = userA) }

            val guardadoIniciado = CompletableDeferred<String>()
            val permitirFinalizar = CompletableDeferred<Unit>()

            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                auth.setProfilePhotoWithSaver(source) { key, value ->
                    guardadoIniciado.complete(key)
                    permitirFinalizar.await()
                    photoStore.save(key, value)
                }
            }

            // Confirmar que el guardado de A ya comenzo.
            assertEquals(keyA, guardadoIniciado.await())

            // Cambiar a B mientras el guardado sigue detenido.
            session.update { Session(user = userB) }
            assertEquals(userB.id, session.current.user?.id)

            // Permitir que termine el guardado iniciado por A.
            permitirFinalizar.complete(Unit)
            job.join()

            assertEquals(userB.id, session.current.user?.id)
            assertNull(session.current.profilePhoto)
            assertTrue(photoStore.fileFor(keyA)?.exists() == true)
            assertNull(photoStore.fileFor(keyB))
        } finally {
            source.delete()
            photoStore.delete(keyA)
            photoStore.delete(keyB)
            db.close()
        }
    }
}
