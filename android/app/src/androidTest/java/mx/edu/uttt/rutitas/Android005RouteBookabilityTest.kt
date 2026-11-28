package mx.edu.uttt.rutitas

import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.TripDirection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Android005RouteBookabilityTest {

    private fun classifiedRoute() = Route(
        id = 89001L,
        origin = "Coyote",
        destination = "UTTT",
        departure = "7:00 AM",
        arrival = "7:30 AM",
        price = 20,
        seats = 2,
        driverName = "Conductor Prueba",
        driverRating = 5.0,
        car = "Automovil",
        stops = listOf("Coyote", "UTTT"),
        status = RouteStatus.ACTIVE,
        ownedByMe = false,
        alternative = false,
        tag = null,
        days = "Lun",
        recurring = false,
        shift = Shift.MATUTINO,
        direction = TripDirection.ENTRADA
    )

    @Test
    fun classifiedActiveRouteWithSeatsIsBookable() {
        assertTrue(classifiedRoute().isBookable)
    }

    @Test
    fun missingShiftBlocksBooking() {
        assertFalse(classifiedRoute().copy(shift = null).isBookable)
    }

    @Test
    fun missingDirectionBlocksBooking() {
        assertFalse(classifiedRoute().copy(direction = null).isBookable)
    }

    @Test
    fun zeroSeatsBlocksBooking() {
        assertFalse(classifiedRoute().copy(seats = 0).isBookable)
    }

    @Test
    fun cancelledRouteBlocksBooking() {
        assertFalse(
            classifiedRoute().copy(
                status = RouteStatus.CANCELLED
            ).isBookable
        )
    }
}