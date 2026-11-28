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
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.TripDirection
import mx.edu.uttt.rutitas.model.UserProfile
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.repository.BookingRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Android005BookingClassificationTest {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var repository: BookingRepository

    @Before
    fun setup() {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

        session = SessionManager()

        session.update {
            Session(
                user = UserProfile(
                    901L,
                    "Pasajero Prueba",
                    "pasajero@uttt.edu.mx",
                    UserType.ALUMNO
                )
            )
        }

        repository = BookingRepository(
            db.bookingDao(),
            db.routeDao(),
            session,
            db
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun createRoute(
        seats: Int = 3,
        shift: String? = "VESPERTINO",
        direction: String? = "SALIDA"
    ): Long {
        return db.routeDao().insert(
            RouteEntity(
                origin = "UTTT",
                destination = "Tula Centro",
                departure = "2:00 PM",
                arrival = "2:30 PM",
                seats = seats,
                price = 20,
                driverName = "Conductor Prueba",
                driverRating = 5.0,
                car = "Automovil",
                stops = "UTTT|Atitalaquia|Tula Centro",
                status = "ACTIVE",
                ownedByMe = false,
                alternative = false,
                tag = null,
                days = "Lun, Mar",
                recurring = false,
                ownerUserId = 501L,
                shift = shift,
                direction = direction
            )
        )
    }

    @Test
    fun bookingUsesDriverClassificationNotPassengerValues() = runBlocking {
        val routeId = createRoute()

        val result = repository.reserve(
            routeId,
            "Atitalaquia",
            1,
            Shift.MATUTINO,
            TripDirection.ENTRADA
        )

        assertTrue(result.toString(), result.isSuccess)

        val booking = db.bookingDao().get(result.getOrThrow())!!

        assertEquals("VESPERTINO", booking.shift)
        assertEquals("SALIDA", booking.direction)
        assertEquals("Atitalaquia", booking.pickup)
        assertEquals(2, db.routeDao().get(routeId)!!.seats)
    }

    @Test
    fun historicalUnclassifiedRouteCannotBeBooked() = runBlocking {
        val routeId = createRoute(
            seats = 2,
            shift = null,
            direction = null
        )

        val result = repository.reserve(routeId, "UTTT", 1)

        assertTrue(result.isFailure)
        assertEquals(2, db.routeDao().get(routeId)!!.seats)
        assertEquals(0, db.bookingDao().countCreated())
    }

    @Test
    fun invalidPickupDoesNotConsumeSeats() = runBlocking {
        val routeId = createRoute()

        val result = repository.reserve(
            routeId,
            "Lugar inexistente",
            1
        )

        assertTrue(result.isFailure)
        assertEquals(3, db.routeDao().get(routeId)!!.seats)
        assertEquals(0, db.bookingDao().countCreated())
    }

    @Test
    fun exceedingCapacityDoesNotCreateBooking() = runBlocking {
        val routeId = createRoute(seats = 2)

        val result = repository.reserve(routeId, "UTTT", 3)

        assertTrue(result.isFailure)
        assertEquals(2, db.routeDao().get(routeId)!!.seats)
        assertEquals(0, db.bookingDao().countCreated())
    }

    @Test
    fun competingForLastSeatOnlyOneBookingSucceeds() = runBlocking {
        val routeId = createRoute(seats = 1)

        val results = listOf(
            async { repository.reserve(routeId, "UTTT", 1) },
            async { repository.reserve(routeId, "UTTT", 1) }
        ).awaitAll()

        assertEquals(1, results.count { it.isSuccess })
        assertEquals(0, db.routeDao().get(routeId)!!.seats)
        assertEquals(1, db.bookingDao().countCreated())
    }
}