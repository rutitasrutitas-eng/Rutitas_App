package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.*
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.network.ApiService
import mx.edu.uttt.rutitas.repository.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit

@RunWith(AndroidJUnit4::class)
class OwnershipHardeningTest {
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var bookings: BookingRepository
    private lateinit var routes: RouteRepository
    private lateinit var driver: DriverRepository
    private lateinit var reviews: ReviewRepository

    private fun login(id: Long) = session.update {
        Session(user = UserProfile(id, "User$id", "$id@uttt.edu.mx", UserType.ALUMNO))
    }

    private fun route(owner: Long, seats: Int = 2) = RouteEntity(
        origin = "Coyote", destination = "UTTT", departure = "7:00 AM", arrival = "7:30 AM",
        price = 20, seats = seats, driverName = "User$owner", driverRating = 5.0,
        car = "Auto", stops = "Coyote|UTTT", status = "ACTIVE", ownedByMe = true,
        alternative = false, tag = null, days = "Lun", recurring = false, ownerUserId = owner
    )

    private fun request(owner: Long, code: String = "REQ-1") = RequestEntity(
        passengerName = "Pasajero", rating = 5.0, pickup = "Coyote", seats = 1,
        status = "PENDING", code = code, ownerUserId = owner
    )

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        session = SessionManager()
        login(101)
        bookings = BookingRepository(db.bookingDao(), db.routeDao(), session, db)
        val api = Retrofit.Builder().baseUrl("https://example.invalid/").build().create(ApiService::class.java)
        routes = RouteRepository(db.routeDao(), api, session)
        driver = DriverRepository(db.routeDao(), db.requestDao(), db.bookingDao(), session, PrivatePhotoStore(context), db)
        reviews = ReviewRepository(db.reviewDao(), session)
    }

    @After fun tearDown() { db.close() }

    @Test fun sessionChangeHidesPrivateBookingsAndOwnRoutes() = runBlocking {
        val routeId = db.routeDao().insert(route(101))
        val bookingId = bookings.reserve(routeId, "Coyote", 1).getOrThrow()
        assertTrue(bookings.bookings.first().any { it.id == bookingId })
        assertTrue(routes.myRoutes.first().any { it.id == routeId })
        login(102)
        assertFalse(bookings.bookings.first().any { it.id == bookingId })
        assertNull(bookings.observe(bookingId).first())
        assertFalse(routes.myRoutes.first().any { it.id == routeId })
        assertTrue(routes.publicRoutes.first().any { it.id == routeId })
        login(101)
        assertTrue(bookings.bookings.first().any { it.id == bookingId })
    }

    @Test fun anotherUserCannotMutateRouteOrBooking() = runBlocking {
        val routeId = db.routeDao().insert(route(101))
        val bookingId = bookings.reserve(routeId, "Coyote", 1).getOrThrow()
        login(102)
        assertTrue(routes.update(routeId, "Otro", "UTTT", "8:00 AM", 9, 10, "Mar", false, 5).isFailure)
        routes.setStatus(routeId, RouteStatus.CANCELLED)
        assertNotNull(driver.startRoute(routeId))
        driver.advanceRoute(routeId, 4)
        driver.finishRoute(routeId)
        driver.cancelRoute(routeId)
        bookings.cancel(bookingId)
        bookings.confirmPending(bookingId)
        bookings.startTrip(bookingId)
        bookings.advance(bookingId, 4)
        bookings.rate(bookingId)
        assertEquals("ACTIVE", db.routeDao().get(routeId)?.status)
        assertEquals(0, db.routeDao().get(routeId)?.progress)
        assertEquals("CONFIRMED", db.bookingDao().get(bookingId)?.status)
        assertEquals(0, db.bookingDao().get(bookingId)?.progress)
        assertFalse(db.bookingDao().get(bookingId)!!.rated)
    }

    @Test fun anotherUserCannotAcceptRejectOrValidateOwnersRequest() = runBlocking {
        val routeA = db.routeDao().insert(route(101))
        val routeB = db.routeDao().insert(route(102))
        db.requestDao().insertAll(listOf(request(101)))
        val reqId = db.requestDao().findByCode("REQ-1")!!.id
        login(102)
        assertFalse(driver.requests.first().any { it.id == reqId })
        assertNotNull(driver.acceptRequest(reqId, routeB))
        driver.rejectRequest(reqId)
        assertEquals("PENDING", db.requestDao().get(reqId)?.status)
        login(101)
        assertNull(driver.acceptRequest(reqId, routeA))
        login(102)
        assertTrue(driver.validateCode(routeB, "REQ-1").isFailure)
    }

    @Test fun bookingCodeIsRouteBoundAndSingleUseAcrossRepositoryInstances() = runBlocking {
        val routeA = db.routeDao().insert(route(101))
        val routeB = db.routeDao().insert(route(101))
        val bookingId = bookings.reserve(routeA, "Coyote", 1).getOrThrow()
        val code = db.bookingDao().get(bookingId)!!.code
        assertTrue(driver.validateCode(routeB, code).isFailure)
        assertTrue(driver.validateCode(routeA, code).isSuccess)
        val second = DriverRepository(db.routeDao(), db.requestDao(), db.bookingDao(), session,
            PrivatePhotoStore(ApplicationProvider.getApplicationContext()), db)
        assertTrue(second.validateCode(routeA, code).isFailure)
    }

    @Test fun concurrentRequestsCannotExceedCapacityAndCancellationIsIdempotent() = runBlocking {
        val routeId = db.routeDao().insert(route(101, seats = 1))
        db.requestDao().insertAll(listOf(request(101, "R-1"), request(101, "R-2")))
        val first = db.requestDao().findByCode("R-1")!!.id
        val second = db.requestDao().findByCode("R-2")!!.id
        val results = listOf(async { driver.acceptRequest(first, routeId) },
            async { driver.acceptRequest(second, routeId) }).awaitAll()
        assertEquals(1, results.count { it == null })
        assertEquals(0, db.routeDao().get(routeId)?.seats)
        val booking = bookings.reserve(routeId, "Coyote", 1)
        assertTrue(booking.isFailure)
        val extraRoute = db.routeDao().insert(route(101, seats = 1))
        val bookingId = bookings.reserve(extraRoute, "Coyote", 1).getOrThrow()
        bookings.cancel(bookingId)
        bookings.cancel(bookingId)
        assertEquals(1, db.routeDao().get(extraRoute)?.seats)
    }

    @Test fun forgedReviewReferenceCannotRateAnotherUsersBooking() = runBlocking {
        val routeId = db.routeDao().insert(route(101))
        val bookingId = bookings.reserve(routeId, "Coyote", 1).getOrThrow()
        login(102)
        assertFalse(reviews.add("booking:$bookingId", ReviewRole.PASSENGER_TO_DRIVER,
            "User102", "User101", 5, emptySet(), "", "Coyote → UTTT"))
        assertNull(db.reviewDao().findByRef("booking:$bookingId"))
    }
}
