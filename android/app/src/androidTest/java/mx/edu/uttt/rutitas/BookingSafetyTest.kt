package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.database.RouteEntity
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.model.UserProfile
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.repository.BookingRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingSafetyTest {
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var bookings: BookingRepository

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .build()
        session = SessionManager()
        session.update { Session(user = UserProfile(101, "A", "a@uttt.edu.mx", UserType.ALUMNO)) }
        bookings = BookingRepository(db.bookingDao(), db.routeDao(), session, db)
    }

    @After fun tearDown() { db.close() }

    private fun route() = RouteEntity(
        origin = "UTTT", destination = "Tula", departure = "8:00 AM", arrival = "8:30 AM",
        price = 20, seats = 1, driverName = "Conductor", driverRating = 5.0, car = "Auto",
        stops = "UTTT|Tula", status = "ACTIVE", ownedByMe = false, alternative = false,
        tag = null, days = "Lun", recurring = false
    )

    @Test fun competingForLastSeatOnlyOneSucceeds() = runBlocking {
        val id = db.routeDao().insert(route())
        val results = listOf(async { bookings.reserve(id, "UTTT", 1) }, async { bookings.reserve(id, "UTTT", 1) }).awaitAll()
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(0, db.routeDao().get(id)!!.seats)
    }

    @Test fun zeroOrNegativeSeatsNeverChangeCapacity() = runBlocking {
        val id = db.routeDao().insert(route())
        assertTrue(bookings.reserve(id, "UTTT", 0).isFailure)
        assertTrue(bookings.reserve(id, "UTTT", -1).isFailure)
        assertEquals(1, db.routeDao().get(id)!!.seats)
    }

    @Test fun cancellingTwiceReturnsSeatsOnce() = runBlocking {
        val routeId = db.routeDao().insert(route())
        val bookingId = bookings.reserve(routeId, "UTTT", 1).getOrThrow()
        bookings.cancel(bookingId)
        bookings.cancel(bookingId)
        assertEquals(1, db.routeDao().get(routeId)!!.seats)
        assertEquals("CANCELLED", db.bookingDao().get(bookingId)!!.status)
    }

    @Test fun anotherUserCannotCancelBooking() = runBlocking {
        val routeId = db.routeDao().insert(route())
        val bookingId = bookings.reserve(routeId, "UTTT", 1).getOrThrow()
        session.update { Session(user = UserProfile(102, "B", "b@uttt.edu.mx", UserType.ALUMNO)) }
        bookings.cancel(bookingId)
        assertEquals(0, db.routeDao().get(routeId)!!.seats)
        assertEquals("CONFIRMED", db.bookingDao().get(bookingId)!!.status)
    }
}
