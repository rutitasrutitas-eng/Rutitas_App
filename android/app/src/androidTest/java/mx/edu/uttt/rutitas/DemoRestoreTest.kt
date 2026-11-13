package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.repository.DriverRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoRestoreTest {

    @Test
    fun demoSeedDoesNotOverwriteOtherUsersData() = runBlocking {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            val booking = DemoData.bookings().first().copy(
                code = "RESERVA-REAL",
                ownerUserId = 101L,
                seeded = false
            )

            val request = DemoData.requests().first().copy(
                passengerName = "Usuario real",
                ownerUserId = 101L
            )

            val review = DemoData.reviews().first().copy(
                id = 1L,
                refKey = "resena-real",
                ownerUserId = 101L
            )

            db.bookingDao().insert(booking)
            db.requestDao().insertAll(listOf(request))
            db.reviewDao().insert(review)

            val session = SessionManager()
            val photoStore = PrivatePhotoStore(context)

            val driverRepo = DriverRepository(
                db.routeDao(),
                db.requestDao(),
                db.bookingDao(),
                session,
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

            seeder.seedIfNeeded()

            withTimeout(10000) {
                seeder.ready.first { it }
            }

            assertEquals("RESERVA-REAL", db.bookingDao().get(1L)?.code)
            assertEquals(101L, db.bookingDao().get(1L)?.ownerUserId)

            assertEquals(
                "Usuario real",
                db.requestDao().get(1L)?.passengerName
            )

            assertNotNull(
                db.reviewDao().findByRef("resena-real")
            )

            assertEquals(4, db.routeDao().count())

        } finally {
            db.close()
        }
    }
}
