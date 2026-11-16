package mx.edu.uttt.rutitas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DriverLocalStore
import mx.edu.uttt.rutitas.model.DocKind
import mx.edu.uttt.rutitas.model.DocStatus
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.model.Vehicle
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DriverPersistenceTest {

    @Test
    fun driverStateSurvivesStoreRecreation() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        val userId = 910001L
        val store = DriverLocalStore(context)

        store.clear(userId)

        try {
            val original = Session(
                user = DemoData.demoUser.copy(id = userId),
                driverStatus = DriverStatus.APPROVED,
                vehicle = Vehicle(
                    brand = "Nissan",
                    model = "Versa",
                    year = "2022",
                    color = "Gris",
                    plates = "TEST-123",
                    seats = 3
                ),
                documents = DocKind.entries.associateWith {
                    DocStatus.COMPLETED
                }
            )

            store.save(userId, original)

            // Simula volver a crear el almacenamiento.
            val reopenedStore = DriverLocalStore(context)

            val restored = reopenedStore.restore(
                Session(user = original.user)
            )

            assertEquals(
                DriverStatus.APPROVED,
                restored.driverStatus
            )
            assertEquals("Nissan", restored.vehicle.brand)
            assertEquals("Versa", restored.vehicle.model)
            assertEquals(3, restored.vehicle.seats)

            DocKind.entries.forEach { kind ->
                assertEquals(
                    DocStatus.COMPLETED,
                    restored.documents[kind]
                )
            }
        } finally {
            store.clear(userId)
        }
    }

    @Test
    fun driverDataIsIsolatedByUser() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        val store = DriverLocalStore(context)
        val userA = 910002L
        val userB = 910003L

        store.clear(userA)
        store.clear(userB)

        try {
            val sessionA = Session(
                user = DemoData.demoUser.copy(id = userA),
                driverStatus = DriverStatus.APPROVED,
                vehicle = Vehicle(
                    brand = "Nissan",
                    model = "Versa",
                    plates = "TEST-A"
                )
            )

            val sessionB = Session(
                user = DemoData.demoUser.copy(id = userB)
            )

            store.save(userA, sessionA)

            val restoredA = store.restore(sessionA)
            val restoredB = store.restore(sessionB)

            assertEquals(
                DriverStatus.APPROVED,
                restoredA.driverStatus
            )

            assertEquals(
                DriverStatus.NONE,
                restoredB.driverStatus
            )

            assertEquals("", restoredB.vehicle.plates)
        } finally {
            store.clear(userA)
            store.clear(userB)
        }
    }

    @Test
    fun deletingDriverDataKeepsOtherAccount() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        val store = DriverLocalStore(context)
        val userA = 910004L
        val userB = 910005L

        store.clear(userA)
        store.clear(userB)

        try {
            val sessionA = Session(
                user = DemoData.demoUser.copy(id = userA),
                driverStatus = DriverStatus.APPROVED
            )

            val sessionB = Session(
                user = DemoData.demoUser.copy(id = userB),
                driverStatus = DriverStatus.APPROVED
            )

            store.save(userA, sessionA)
            store.save(userB, sessionB)

            store.clear(userA)

            assertEquals(
                DriverStatus.NONE,
                store.restore(Session(user = sessionA.user)).driverStatus
            )

            assertEquals(
                DriverStatus.APPROVED,
                store.restore(Session(user = sessionB.user)).driverStatus
            )
        } finally {
            store.clear(userA)
            store.clear(userB)
        }
    }

    @Test
    fun driverPhotosAreRestoredAndIsolated() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            mx.edu.uttt.rutitas.database.AppDatabase::class.java
        ).build()

        val photoStore = mx.edu.uttt.rutitas.data.PrivatePhotoStore(context)
        val session = mx.edu.uttt.rutitas.data.SessionManager()
        val userA = 910006L
        val userB = 910007L

        val dir = java.io.File(context.filesDir, "private_photos")
            .apply { mkdirs() }

        val photoA = java.io.File(dir, "user_${userA}_identity.jpg")
        val photoB = java.io.File(dir, "user_${userB}_identity.jpg")

        try {
            // Archivos ficticios: no contienen documentos personales.
            photoA.writeBytes(byteArrayOf(1, 2, 3))
            photoB.writeBytes(byteArrayOf(4, 5, 6))

            session.update {
                Session(user = DemoData.demoUser.copy(id = userA))
            }

            // Simula volver a crear el repositorio.
            val repo = mx.edu.uttt.rutitas.repository.DriverRepository(
                db.routeDao(),
                db.requestDao(),
                db.bookingDao(),
                session,
                DriverLocalStore(context),
                photoStore,
                db
            )

            repo.restorePhotos()

            val photosA = repo.photos.first()

            assertTrue(photosA.containsKey("identity"))
            assertEquals(photoA.absolutePath, photosA["identity"]?.absolutePath)

            // Cambiar de cuenta no debe mostrar fotos de A.
            session.update {
                Session(user = DemoData.demoUser.copy(id = userB))
            }

            repo.restorePhotos()

            val photosB = repo.photos.first()

            assertTrue(photosB.containsKey("identity"))
            assertEquals(photoB.absolutePath, photosB["identity"]?.absolutePath)
            assertFalse(photosB.values.any { it.absolutePath == photoA.absolutePath })

        } finally {
            photoA.delete()
            photoB.delete()
            db.close()
        }
    }
}
