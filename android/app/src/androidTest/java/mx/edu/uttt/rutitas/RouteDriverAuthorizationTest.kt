package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.TripDirection
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.model.UserProfile
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.network.ApiService
import mx.edu.uttt.rutitas.repository.RouteRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit

@RunWith(AndroidJUnit4::class)
class RouteDriverAuthorizationTest {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var routes: RouteRepository

    @Before
    fun setup() {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

        session = SessionManager()

        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .build()
            .create(ApiService::class.java)

        routes = RouteRepository(
            db.routeDao(),
            api,
            session,
            db.bookingDao()
        )
    }

    @After
    fun cleanup() {
        db.close()
    }

    private fun login(
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

    private suspend fun publishRoute() = routes.publishClassified(
        origin = "Coyote",
        destination = "UTTT",
        departure = "7:00 AM",
        seats = 3,
        price = 20,
        days = "Lun, Mar",
        recurring = false,
        waitMinutes = 5,
        shift = Shift.MATUTINO,
        direction = TripDirection.ENTRADA
    )

    private suspend fun updateRoute(
        id: Long,
        seats: Int = 3
    ) = routes.updateClassified(
        id = id,
        origin = "Coyote",
        destination = "UTTT",
        departure = "8:00 AM",
        seats = seats,
        price = 25,
        days = "Lun, Mar",
        recurring = false,
        waitMinutes = 5,
        shift = Shift.MATUTINO,
        direction = TripDirection.ENTRADA
    )

    @Test
    fun unapprovedStudentCannotPublishOrEdit() = runBlocking {
        login(101L, DriverStatus.APPROVED)

        val routeId = publishRoute().getOrThrow()

        login(101L, DriverStatus.NONE)

        assertTrue(publishRoute().isFailure)
        assertTrue(updateRoute(routeId).isFailure)

        val saved = db.routeDao().get(routeId)!!
        assertEquals(20, saved.price)
        assertEquals("7:00 AM", saved.departure)
        assertEquals(1, db.routeDao().count())
    }

    @Test
    fun approvedStudentCanPublishAndEditOwnRoute() = runBlocking {
        login(101L, DriverStatus.APPROVED)

        val routeId = publishRoute().getOrThrow()

        assertEquals(
            101L,
            db.routeDao().get(routeId)?.ownerUserId
        )

        assertTrue(updateRoute(routeId).isSuccess)

        val updated = db.routeDao().get(routeId)!!
        assertEquals(25, updated.price)
        assertEquals("8:00 AM", updated.departure)
        assertEquals(3, updated.seats)
    }

    @Test
    fun approvedDriverCannotEditAnotherUsersRoute() = runBlocking {
        login(101L, DriverStatus.APPROVED)

        val routeId = publishRoute().getOrThrow()

        login(202L, DriverStatus.APPROVED)

        assertTrue(updateRoute(routeId).isFailure)

        val saved = db.routeDao().get(routeId)!!
        assertEquals(101L, saved.ownerUserId)
        assertEquals(20, saved.price)
        assertEquals("7:00 AM", saved.departure)
    }

    @Test
    fun approvedDriverCannotRestoreOccupiedSeatsByEditing() = runBlocking {
        login(101L, DriverStatus.APPROVED)

        val routeId = publishRoute().getOrThrow()

        val booking = DemoData.bookings().first().copy(
            id = 910101L,
            routeId = routeId,
            ownerUserId = 202L,
            seats = 1,
            status = "CONFIRMED",
            seeded = false
        )

        db.bookingDao().insert(booking)

        assertEquals(
            1,
            db.routeDao().decrementSeatsIfAvailable(routeId, 1)
        )

        assertEquals(2, db.routeDao().get(routeId)?.seats)

        assertTrue(updateRoute(routeId, seats = 3).isFailure)

        assertEquals(2, db.routeDao().get(routeId)?.seats)
        assertEquals(
            "CONFIRMED",
            db.bookingDao().get(910101L)?.status
        )
    }
}