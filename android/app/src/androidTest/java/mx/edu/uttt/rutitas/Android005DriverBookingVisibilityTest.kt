package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.repository.BookingRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Android005DriverBookingVisibilityTest {

    private fun database(): AppDatabase {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        return Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    private fun login(
        session: SessionManager,
        id: Long,
        status: DriverStatus
    ) {
        session.update {
            Session(
                user = UserProfile(
                    id,
                    "Usuario$id",
                    "$id@uttt.edu.mx",
                    UserType.ALUMNO
                ),
                driverStatus = status
            )
        }
    }

    private suspend fun prepare(db: AppDatabase) {
        val route = DemoData.routes().first()
        val booking = DemoData.bookings().first()

        db.routeDao().insert(
            route.copy(
                id = 94001L,
                ownerUserId = 101L,
                status = "ACTIVE",
                alternative = false,
                shift = "MATUTINO",
                direction = "ENTRADA"
            )
        )

        db.routeDao().insert(
            route.copy(
                id = 94002L,
                ownerUserId = 202L,
                status = "ACTIVE",
                alternative = false,
                shift = "MATUTINO",
                direction = "ENTRADA"
            )
        )

        db.bookingDao().insert(
            booking.copy(
                id = 95001L,
                routeId = 94001L,
                ownerUserId = 301L,
                status = "CONFIRMED",
                seeded = false
            )
        )

        db.bookingDao().insert(
            booking.copy(
                id = 95002L,
                routeId = 94002L,
                ownerUserId = 302L,
                status = "CONFIRMED",
                seeded = false
            )
        )

        db.bookingDao().insert(
            booking.copy(
                id = 95003L,
                routeId = 94001L,
                ownerUserId = 303L,
                status = "CANCELLED",
                seeded = false
            )
        )
    }

    @Test
    fun ownerOnlySeesConfirmedBookingsOfOwnRoute() = runBlocking {
        val db = database()
        try {
            prepare(db)
            val session = SessionManager()
            val repo = BookingRepository(
                db.bookingDao(), db.routeDao(), session, db
            )

            login(session, 101L, DriverStatus.APPROVED)

            val own = repo.confirmedForDriverRoute(94001L).first()
            assertEquals(1, own.size)
            assertEquals(95001L, own.first().id)

            val foreign = repo.confirmedForDriverRoute(94002L).first()
            assertTrue(foreign.isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun otherDriverCannotAccessPreviousOwnersBookings() = runBlocking {
        val db = database()
        try {
            prepare(db)
            val session = SessionManager()
            val repo = BookingRepository(
                db.bookingDao(), db.routeDao(), session, db
            )

            login(session, 101L, DriverStatus.APPROVED)
            assertEquals(
                1,
                repo.confirmedForDriverRoute(94001L).first().size
            )

            login(session, 202L, DriverStatus.APPROVED)

            assertTrue(
                repo.confirmedForDriverRoute(94001L).first().isEmpty()
            )

            val own = repo.confirmedForDriverRoute(94002L).first()
            assertEquals(1, own.size)
            assertEquals(95002L, own.first().id)
        } finally {
            db.close()
        }
    }

    @Test
    fun passengerAndUnapprovedDriverCannotReadBookings() = runBlocking {
        val db = database()
        try {
            prepare(db)
            val session = SessionManager()
            val repo = BookingRepository(
                db.bookingDao(), db.routeDao(), session, db
            )

            login(session, 301L, DriverStatus.NONE)

            assertTrue(
                repo.confirmedForDriverRoute(94001L).first().isEmpty()
            )

            login(session, 101L, DriverStatus.NONE)

            assertTrue(
                repo.confirmedForDriverRoute(94001L).first().isEmpty()
            )

            login(session, 101L, DriverStatus.APPROVED)

            assertEquals(
                1,
                repo.confirmedForDriverRoute(94001L).first().size
            )
        } finally {
            db.close()
        }
    }
}