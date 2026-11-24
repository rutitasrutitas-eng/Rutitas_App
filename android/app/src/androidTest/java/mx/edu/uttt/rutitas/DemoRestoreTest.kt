package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.DriverLocalStore
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
                DriverLocalStore(context),
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

    @Test
    fun restoreDemoAccountsRecoversBothAccountsAndPreservesOtherData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val prefs = context.getSharedPreferences(
            "rutitas_app03_restore_instrumentation",
            android.content.Context.MODE_PRIVATE
        )

        val isolatedContext = object : android.content.ContextWrapper(context) {
            override fun getSharedPreferences(
                name: String?,
                mode: Int
            ): android.content.SharedPreferences {
                return if (name == "rutitas_prefs") {
                    prefs
                } else {
                    super.getSharedPreferences(name, mode)
                }
            }
        }

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            assertTrue(prefs.edit().clear().commit())

            assertTrue(
                prefs.edit()
                    .putBoolean("deleted_${DemoData.DEMO_EMAIL}", true)
                    .putBoolean("deleted_${DemoData.BLOCKED_EMAIL}", true)
                    .commit()
            )

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

            val auth = mx.edu.uttt.rutitas.repository.AuthRepository(
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

            val otherBooking = DemoData.bookings().first().copy(
                code = "APP03-PRESERVE-BOOKING",
                ownerUserId = 910001L,
                seeded = false
            )

            val bookingId = db.bookingDao().insert(otherBooking)

            assertTrue(
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isFailure
            )

            assertTrue(
                auth.login(
                    DemoData.BLOCKED_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).isFailure
            )

            auth.restoreDemoAccounts()

            assertFalse(
                prefs.contains("deleted_${DemoData.DEMO_EMAIL}")
            )

            assertFalse(
                prefs.contains("deleted_${DemoData.BLOCKED_EMAIL}")
            )

            assertEquals(
                "APP03-PRESERVE-BOOKING",
                db.bookingDao().get(bookingId)?.code
            )

            assertEquals(
                910001L,
                db.bookingDao().get(bookingId)?.ownerUserId
            )

            assertEquals(4, db.routeDao().count())

            assertEquals(
                false,
                auth.login(
                    DemoData.DEMO_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).getOrThrow()
            )

            assertEquals(
                DemoData.demoUser.id,
                session.current.user?.id
            )

            assertEquals(
                true,
                auth.login(
                    DemoData.BLOCKED_EMAIL,
                    DemoData.DEMO_PASSWORD
                ).getOrThrow()
            )

            assertEquals(
                DemoData.blockedUser.id,
                session.current.user?.id
            )
        } finally {
            prefs.edit().clear().commit()
            db.close()
        }
    }
}
