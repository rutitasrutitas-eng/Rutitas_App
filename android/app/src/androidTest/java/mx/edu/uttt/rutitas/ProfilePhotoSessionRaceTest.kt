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
                auth.setProfilePhotoWithSaver(source) { key, value, version ->
                    guardadoIniciado.complete(key)
                    permitirFinalizar.await()
                    photoStore.save(key, value, version)
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

    @Test
    fun photoFinishingAfterAccountDeletionMustNotRemain() = runBlocking {
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

        val userA = DemoData.demoUser.copy(id = 980003L)
        val keyA = "profile_${userA.id}"

        val source = File.createTempFile(
            "app02-delete-test-", ".jpg", context.cacheDir
        )

        try {
            photoStore.delete(keyA)

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

            val started = CompletableDeferred<String>()
            val resumeSave = CompletableDeferred<Unit>()

            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                auth.setProfilePhotoWithSaver(source) { key, value, version ->
                    started.complete(key)
                    resumeSave.await()
                    photoStore.save(key, value, version)
                }
            }

            assertEquals(keyA, started.await())

            auth.deleteAccount()

            assertNull(session.current.user)

            resumeSave.complete(Unit)
            job.join()

            assertNull(
                "La foto reaparecio despues de eliminar la cuenta",
                photoStore.fileFor(keyA)
            )
        } finally {
            source.delete()
            photoStore.delete(keyA)
            db.close()
        }
    }

    @Test
    fun oldPhotoSaveMustNotSurviveDemoAccountRestoration() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val isolatedDir = File(
            context.cacheDir,
            "rutitas_app02_restore_race"
        ).apply { mkdirs() }

        val isolatedContext = object : android.content.ContextWrapper(context) {
            override fun getFilesDir(): File = isolatedDir

            override fun getSharedPreferences(
                name: String?,
                mode: Int
            ): android.content.SharedPreferences {
                return context.getSharedPreferences(
                    "rutitas_app02_restore_race_${name}",
                    mode
                )
            }
        }

        val db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

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

        val user = DemoData.demoUser
        val key = "profile_${user.id}"

        val source = File.createTempFile(
            "app02-old-save-", ".jpg", isolatedDir
        )

        val bitmap = Bitmap.createBitmap(
            32, 32, Bitmap.Config.ARGB_8888
        )

        try {
            FileOutputStream(source).use { output ->
                assertTrue(
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                )
            }
        } finally {
            bitmap.recycle()
        }

        try {
            photoStore.unblockAccountPhotos(user.id)
            photoStore.delete(key)

            session.update { Session(user = user) }

            val started = CompletableDeferred<Unit>()
            val resumeSave = CompletableDeferred<Unit>()

            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                auth.setProfilePhotoWithSaver(source) { photoKey, value, version ->
                    started.complete(Unit)
                    resumeSave.await()
                    photoStore.save(photoKey, value, version)
                }
            }

            started.await()

            auth.deleteAccount()

            assertNull(photoStore.fileFor(key))

            auth.restoreDemoAccounts()

            resumeSave.complete(Unit)
            job.join()

            assertNull(
                "Un guardado anterior a la baja reaparecio tras restaurar DEMO",
                photoStore.fileFor(key)
            )
        } finally {
            source.delete()
            photoStore.blockAccountPhotos(user.id)
            photoStore.unblockAccountPhotos(user.id)
            isolatedDir.deleteRecursively()
            db.close()
        }
    }
}
