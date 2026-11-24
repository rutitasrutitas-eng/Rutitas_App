package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DriverLocalStore
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.database.RequestEntity
import mx.edu.uttt.rutitas.model.Session
import mx.edu.uttt.rutitas.model.UserProfile
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.repository.DriverRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class App04CancellationRegressionTest {

    @Test
    fun cancellationOnlyAffectsSelectedRouteAndIsIdempotent() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(
            ctx, AppDatabase::class.java
        ).build()

        try {
            val session = SessionManager()
            session.update {
                Session(
                    user = UserProfile(
                        101L, "Conductor 101",
                        "101@uttt.edu.mx", UserType.ALUMNO
                    )
                )
            }

            val driver = DriverRepository(
                db.routeDao(),
                db.requestDao(),
                db.bookingDao(),
                session,
                DriverLocalStore(ctx),
                PrivatePhotoStore(ctx),
                db
            )

            val baseRoute = DemoData.routes().first()

            db.routeDao().insert(
                baseRoute.copy(
                    id = 92001L,
                    ownerUserId = 101L,
                    status = "ACTIVE",
                    seats = 3
                )
            )

            db.routeDao().insert(
                baseRoute.copy(
                    id = 92002L,
                    ownerUserId = 101L,
                    status = "ACTIVE",
                    seats = 3
                )
            )

            db.routeDao().insert(
                baseRoute.copy(
                    id = 92003L,
                    ownerUserId = 102L,
                    status = "ACTIVE",
                    seats = 3
                )
            )

            fun req(
                id: Long,
                route: Long,
                owner: Long,
                status: String
            ) = RequestEntity(
                id = id,
                passengerName = "Pasajero $id",
                rating = 5.0,
                pickup = "Coyote",
                seats = 1,
                status = status,
                routeId = route,
                code = "REQ-$id",
                ownerUserId = owner
            )

            db.requestDao().insertAll(
                listOf(
                    req(93001L, 92001L, 101L, "PENDING"),
                    req(93002L, 92001L, 101L, "ACCEPTED"),
                    req(93003L, 92002L, 101L, "PENDING"),
                    req(93004L, 92003L, 102L, "ACCEPTED"),
                    req(93005L, 92001L, 102L, "PENDING"),
                    req(93006L, 92001L, 101L, "REJECTED")
                )
            )

            val bookingBase = DemoData.bookings().first()

            db.bookingDao().insert(
                bookingBase.copy(
                    id = 94001L,
                    routeId = 92001L,
                    status = "CONFIRMED",
                    ownerUserId = 201L,
                    seeded = false
                )
            )

            db.bookingDao().insert(
                bookingBase.copy(
                    id = 94002L,
                    routeId = 92002L,
                    status = "CONFIRMED",
                    ownerUserId = 202L,
                    seeded = false
                )
            )

            driver.cancelRoute(92001L)

            assertEquals("CANCELLED", db.routeDao().get(92001L)?.status)
            assertEquals("ACTIVE", db.routeDao().get(92002L)?.status)
            assertEquals("ACTIVE", db.routeDao().get(92003L)?.status)

            assertEquals("CANCELLED", db.requestDao().get(93001L)?.status)
            assertEquals("CANCELLED", db.requestDao().get(93002L)?.status)
            assertEquals("PENDING", db.requestDao().get(93003L)?.status)
            assertEquals("ACCEPTED", db.requestDao().get(93004L)?.status)
            assertEquals("PENDING", db.requestDao().get(93005L)?.status)
            assertEquals("REJECTED", db.requestDao().get(93006L)?.status)

            assertEquals("CANCELLED", db.bookingDao().get(94001L)?.status)
            assertEquals("CONFIRMED", db.bookingDao().get(94002L)?.status)

            val seats = db.routeDao().get(92001L)?.seats

            driver.cancelRoute(92001L)

            assertEquals("CANCELLED", db.requestDao().get(93001L)?.status)
            assertEquals("CANCELLED", db.requestDao().get(93002L)?.status)
            assertEquals("CANCELLED", db.bookingDao().get(94001L)?.status)
            assertEquals(seats, db.routeDao().get(92001L)?.seats)

        } finally {
            db.close()
        }
    }
}