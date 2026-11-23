package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
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
class RutitasStabilizationDaoTest {
    @Test
    fun routeEditDoesNotRestoreSeatsOccupiedByBooking() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        try {
            val route = DemoData.routes().first().copy(id = 70001L, ownerUserId = 1001L, seats = 3, status = "ACTIVE")
            db.routeDao().insert(route)
            val booking = DemoData.bookings().first().copy(
                id = 71001L, routeId = route.id, ownerUserId = 2002L,
                seats = 1, status = "CONFIRMED", seeded = false
            )
            db.bookingDao().insert(booking)
            assertEquals(1, db.routeDao().decrementSeatsIfAvailable(route.id, 1))
            val changed = db.routeDao().updateDetailsIfSafe(
                route.id, 1001L, route.origin, route.destination, route.departure,
                route.arrival, 3, route.price, route.days, route.recurring,
                route.waitMinutes, route.stops
            )
            assertEquals(0, changed)
            assertEquals(2, db.routeDao().get(route.id)?.seats)
        } finally { db.close() }
    }

    @Test
    fun cancelBookingsOnlyForSelectedRouteAndIsIdempotent() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        try {
            val firstRoute = DemoData.routes().first().copy(id = 70002L)
            val otherRoute = firstRoute.copy(id = 70003L)
            db.routeDao().insert(firstRoute)
            db.routeDao().insert(otherRoute)
            val seedBooking = DemoData.bookings().first()
            db.bookingDao().insert(seedBooking.copy(id = 71002L, routeId = 70002L, status = "CONFIRMED"))
            db.bookingDao().insert(seedBooking.copy(id = 71003L, routeId = 70003L, status = "PENDING"))
            assertEquals(1, db.bookingDao().cancelActiveForRoute(70002L))
            assertEquals(0, db.bookingDao().cancelActiveForRoute(70002L))
            assertEquals("CANCELLED", db.bookingDao().get(71002L)?.status)
            assertEquals("PENDING", db.bookingDao().get(71003L)?.status)
        } finally { db.close() }
    }

    @Test
    fun demoResetReplenishesMissingRowsWhilePreservingExistingRoute() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        try {
            val route = DemoData.routes().first()
            db.routeDao().insert(route)
            val session = SessionManager()
            val driverRepo = DriverRepository(
                db.routeDao(), db.requestDao(), db.bookingDao(), session,
                DriverLocalStore(ctx), PrivatePhotoStore(ctx), db
            )
            val seeder = DemoSeeder(
                db.routeDao(), db.bookingDao(), db.requestDao(), db.reviewDao(),
                driverRepo, session
            )
            seeder.reset()
            assertNotNull(db.routeDao().get(route.id))
            assertNotNull(db.bookingDao().get(DemoData.bookings().first().id))
            assertNotNull(db.requestDao().get(DemoData.requests().first().id))
            assertNotNull(db.reviewDao().findByRef(DemoData.reviews().first().refKey))
        } finally { db.close() }
    }
}
