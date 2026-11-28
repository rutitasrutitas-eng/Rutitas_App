package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.database.AppDatabase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Android005AtomicRouteUpdateTest {

    private fun database(): AppDatabase {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()
        return Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    private suspend fun prepareRoute(db: AppDatabase, id: Long) {
        db.routeDao().insert(
            DemoData.routes().first().copy(
                id = id,
                ownerUserId = 101L,
                status = "ACTIVE",
                origin = "Coyote",
                destination = "UTTT",
                departure = "7:00 AM",
                seats = 3,
                price = 20,
                shift = null,
                direction = null
            )
        )
    }

    private suspend fun save(
        db: AppDatabase,
        id: Long,
        owner: Long = 101L,
        shift: String = "MATUTINO",
        direction: String = "ENTRADA"
    ): Int = db.routeDao().updateRouteWithClassificationIfSafe(
        id = id,
        ownerId = owner,
        origin = "Tula Centro",
        destination = "UTTT",
        departure = "8:00 AM",
        arrival = "8:30 AM",
        seats = 3,
        price = 25,
        days = "Lun, Mar",
        recurring = false,
        waitMinutes = 10,
        stops = "Tula Centro|UTTT",
        shift = shift,
        direction = direction
    )

    @Test
    fun ownerSavesDetailsAndClassificationTogether() = runBlocking {
        val db = database()
        try {
            prepareRoute(db, 84001L)

            assertEquals(1, save(db, 84001L))

            val route = db.routeDao().get(84001L)!!
            assertEquals("Tula Centro", route.origin)
            assertEquals("UTTT", route.destination)
            assertEquals("8:00 AM", route.departure)
            assertEquals(3, route.seats)
            assertEquals(25, route.price)
            assertEquals("MATUTINO", route.shift)
            assertEquals("ENTRADA", route.direction)
        } finally {
            db.close()
        }
    }

    @Test
    fun foreignOwnerCannotChangeAnyField() = runBlocking {
        val db = database()
        try {
            prepareRoute(db, 84002L)

            assertEquals(0, save(db, 84002L, owner = 202L))

            val route = db.routeDao().get(84002L)!!
            assertEquals("Coyote", route.origin)
            assertEquals("7:00 AM", route.departure)
            assertEquals(20, route.price)
            assertNull(route.shift)
            assertNull(route.direction)
        } finally {
            db.close()
        }
    }

    @Test
    fun activeBookingBlocksEntireUpdate() = runBlocking {
        val db = database()
        try {
            prepareRoute(db, 84003L)

            val booking = DemoData.bookings().first().copy(
                id = 85003L,
                routeId = 84003L,
                status = "CONFIRMED",
                seeded = false
            )
            db.bookingDao().insert(booking)

            assertEquals(0, save(db, 84003L))

            val route = db.routeDao().get(84003L)!!
            assertEquals("Coyote", route.origin)
            assertEquals("7:00 AM", route.departure)
            assertEquals(3, route.seats)
            assertEquals(20, route.price)
            assertNull(route.shift)
            assertNull(route.direction)
        } finally {
            db.close()
        }
    }
}