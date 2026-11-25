package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.database.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class App05DriverRequestRejectionTest {

    @Test
    fun rejectionOnlyChangesAuthorizedPendingRequests() = runBlocking {
        val context =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            val driverId = 1001L
            val anotherDriverId = 2002L
            val base = DemoData.requests().first()

            val demo = base.copy(
                id = 98001L,
                ownerUserId = null,
                routeId = 0L,
                status = "PENDING"
            )

            val own = base.copy(
                id = 98002L,
                ownerUserId = driverId,
                routeId = 98020L,
                status = "PENDING"
            )

            val foreign = base.copy(
                id = 98003L,
                ownerUserId = anotherDriverId,
                routeId = 98030L,
                status = "PENDING"
            )

            val alreadyAccepted = base.copy(
                id = 98004L,
                ownerUserId = driverId,
                routeId = 98020L,
                status = "ACCEPTED"
            )

            val unassignedToRoute = base.copy(
                id = 98005L,
                ownerUserId = null,
                routeId = 98020L,
                status = "PENDING"
            )

            db.requestDao().insertAll(
                listOf(
                    demo,
                    own,
                    foreign,
                    alreadyAccepted,
                    unassignedToRoute
                )
            )

            val route = DemoData.routes().first().copy(
                id = 98020L,
                ownerUserId = driverId,
                status = "ACTIVE",
                seats = 3
            )

            db.routeDao().insert(route)

            val dao = db.requestDao()

            // Solicitud DEMO pendiente sin propietario.
            assertEquals(1, dao.rejectIfPending(demo.id, driverId))
            assertEquals("REJECTED", dao.get(demo.id)?.status)

            // No se puede rechazar nuevamente.
            assertEquals(0, dao.rejectIfPending(demo.id, driverId))

            // Solicitud propia pendiente.
            assertEquals(1, dao.rejectIfPending(own.id, driverId))
            assertEquals("REJECTED", dao.get(own.id)?.status)

            // Una cuenta distinta no puede rechazarla.
            assertEquals(
                0,
                dao.rejectIfPending(foreign.id, driverId)
            )
            assertEquals("PENDING", dao.get(foreign.id)?.status)

            // Su propietario legitimo si puede rechazarla.
            assertEquals(
                1,
                dao.rejectIfPending(foreign.id, anotherDriverId)
            )

            // No se alteran solicitudes ya aceptadas.
            assertEquals(
                0,
                dao.rejectIfPending(alreadyAccepted.id, driverId)
            )
            assertEquals(
                "ACCEPTED",
                dao.get(alreadyAccepted.id)?.status
            )

            // Un registro sin propietario con ruta asignada
            // no se considera una solicitud DEMO libre.
            assertEquals(
                0,
                dao.rejectIfPending(unassignedToRoute.id, driverId)
            )
            assertEquals(
                "PENDING",
                dao.get(unassignedToRoute.id)?.status
            )

            // Rechazar solicitudes nunca modifica asientos.
            assertEquals(3, db.routeDao().get(route.id)?.seats)

        } finally {
            db.close()
        }
    }
}