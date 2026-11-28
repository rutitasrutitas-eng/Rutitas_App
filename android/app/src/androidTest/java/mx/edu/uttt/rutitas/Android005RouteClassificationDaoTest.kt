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
class Android005RouteClassificationDaoTest {

    private fun database(): AppDatabase {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        return Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    private suspend fun insertRoute(
        db: AppDatabase,
        id: Long,
        owner: Long? = 101L
    ) {
        val route = DemoData.routes().first().copy(
            id = id,
            ownerUserId = owner,
            status = "ACTIVE",
            shift = null,
            direction = null
        )
        db.routeDao().insert(route)
    }

    @Test
    fun ownerCanClassifyRouteWithoutActiveReservations() = runBlocking {
        val db = database()
        try {
            insertRoute(db, 81001L)

            val changed = db.routeDao().updateClassificationIfSafe(
                81001L, 101L, "MATUTINO", "ENTRADA"
            )

            assertEquals(1, changed)

            val saved = db.routeDao().get(81001L)!!
            assertEquals("MATUTINO", saved.shift)
            assertEquals("ENTRADA", saved.direction)
        } finally {
            db.close()
        }
    }

    @Test
    fun foreignOwnerAndHistoricalRouteCannotBeClassified() = runBlocking {
        val db = database()
        try {
            insertRoute(db, 81002L, 101L)
            insertRoute(db, 81003L, null)

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81002L, 202L, "VESPERTINO", "SALIDA"
                )
            )

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81003L, 101L, "VESPERTINO", "SALIDA"
                )
            )

            assertNull(db.routeDao().get(81002L)?.shift)
            assertNull(db.routeDao().get(81003L)?.shift)
        } finally {
            db.close()
        }
    }

    @Test
    fun activeBookingPreventsClassificationChange() = runBlocking {
        val db = database()
        try {
            insertRoute(db, 81004L)

            val booking = DemoData.bookings().first().copy(
                id = 82004L,
                routeId = 81004L,
                status = "CONFIRMED",
                seeded = false
            )
            db.bookingDao().insert(booking)

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81004L, 101L, "MIXTO", "SALIDA"
                )
            )

            assertNull(db.routeDao().get(81004L)?.shift)
            assertNull(db.routeDao().get(81004L)?.direction)
        } finally {
            db.close()
        }
    }

    @Test
    fun acceptedRequestPreventsClassificationChange() = runBlocking {
        val db = database()
        try {
            insertRoute(db, 81005L)

            val request = DemoData.requests().first().copy(
                id = 83005L,
                routeId = 81005L,
                status = "ACCEPTED"
            )
            db.requestDao().insertAll(listOf(request))

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81005L, 101L, "VESPERTINO", "ENTRADA"
                )
            )

            assertNull(db.routeDao().get(81005L)?.shift)
        } finally {
            db.close()
        }
    }

    @Test
    fun invalidValuesAndCancelledRouteAreRejected() = runBlocking {
        val db = database()
        try {
            insertRoute(db, 81006L)

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81006L, 101L, "NO_VALIDO", "ENTRADA"
                )
            )

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81006L, 101L, "MATUTINO", "NO_VALIDO"
                )
            )

            db.routeDao().cancelRouteIfOpen(81006L, 101L)

            assertEquals(
                0,
                db.routeDao().updateClassificationIfSafe(
                    81006L, 101L, "MATUTINO", "ENTRADA"
                )
            )

            assertNull(db.routeDao().get(81006L)?.shift)
        } finally {
            db.close()
        }
    }
}