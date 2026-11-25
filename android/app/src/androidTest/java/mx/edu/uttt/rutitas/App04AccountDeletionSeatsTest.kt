package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.database.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class App04AccountDeletionSeatsTest {

    @Test
    fun accountDeletionRestoresDemoSeatsExactlyOnce() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(
            ctx,
            AppDatabase::class.java
        ).build()

        try {
            val deletedUserId = 2002L
            val otherOwnerId = 1001L
            val foreignPassengerId = 3003L

            val baseRoute = DemoData.routes().first()

            val demoRoute = baseRoute.copy(
                id = 95001L,
                ownerUserId = null,
                status = "ACTIVE",
                seats = 2
            )

            val deletedUserRoute = baseRoute.copy(
                id = 95002L,
                ownerUserId = deletedUserId,
                status = "ACTIVE",
                seats = 2
            )

            val otherRoute = baseRoute.copy(
                id = 95003L,
                ownerUserId = otherOwnerId,
                status = "ACTIVE",
                seats = 2
            )

            db.routeDao().insert(demoRoute)
            db.routeDao().insert(deletedUserRoute)
            db.routeDao().insert(otherRoute)

            val baseBooking = DemoData.bookings().first()

            db.bookingDao().insert(
                baseBooking.copy(
                    id = 96001L,
                    code = "APP04-DEMO",
                    routeId = demoRoute.id,
                    ownerUserId = deletedUserId,
                    status = "CONFIRMED",
                    seats = 1,
                    seeded = false
                )
            )

            db.bookingDao().insert(
                baseBooking.copy(
                    id = 96002L,
                    code = "APP04-OWN",
                    routeId = deletedUserRoute.id,
                    ownerUserId = deletedUserId,
                    status = "CONFIRMED",
                    seats = 1,
                    seeded = false
                )
            )

            db.bookingDao().insert(
                baseBooking.copy(
                    id = 96003L,
                    code = "APP04-OTHER",
                    routeId = otherRoute.id,
                    ownerUserId = deletedUserId,
                    status = "CONFIRMED",
                    seats = 1,
                    seeded = false
                )
            )

            db.bookingDao().insert(
                baseBooking.copy(
                    id = 96004L,
                    code = "APP04-FOREIGN",
                    routeId = demoRoute.id,
                    ownerUserId = foreignPassengerId,
                    status = "CONFIRMED",
                    seats = 1,
                    seeded = false
                )
            )

            suspend fun removePassengerBookings() {
                db.withTransaction {
                    db.bookingDao().activeForOwner(deletedUserId)
                        .forEach { booking ->
                            db.routeDao().restoreSeatsForAccountDeletion(
                                booking.routeId,
                                booking.seats,
                                deletedUserId
                            )
                        }

                    db.bookingDao().deleteForOwner(deletedUserId)
                }
            }

            removePassengerBookings()

            // Ruta DEMO sin propietario: recupera un asiento.
            assertEquals(3, db.routeDao().get(95001L)?.seats)

            // Ruta del usuario eliminado: no libera cupos.
            assertEquals(2, db.routeDao().get(95002L)?.seats)

            // Ruta de otro propietario: recupera un asiento.
            assertEquals(3, db.routeDao().get(95003L)?.seats)

            // La reserva de otro pasajero permanece intacta.
            assertEquals(
                "CONFIRMED",
                db.bookingDao().get(96004L)?.status
            )

            assertEquals(null, db.bookingDao().get(96001L))
            assertEquals(null, db.bookingDao().get(96002L))
            assertEquals(null, db.bookingDao().get(96003L))

            // Una segunda eliminacion no debe liberar de nuevo.
            removePassengerBookings()

            assertEquals(3, db.routeDao().get(95001L)?.seats)
            assertEquals(2, db.routeDao().get(95002L)?.seats)
            assertEquals(3, db.routeDao().get(95003L)?.seats)

            assertEquals(
                "CONFIRMED",
                db.bookingDao().get(96004L)?.status
            )
        } finally {
            db.close()
        }
    }
}