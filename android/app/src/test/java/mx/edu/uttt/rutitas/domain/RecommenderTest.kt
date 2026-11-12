package mx.edu.uttt.rutitas.domain

import mx.edu.uttt.rutitas.model.*
import org.junit.Assert.*
import org.junit.Test

class RecommenderTest {
    private fun route(id: Long, origin: String, arrival: String) = Route(
        id = id, origin = origin, destination = "UTTT", departure = "6:00 AM", arrival = arrival,
        price = 10, seats = 2, driverName = "Prueba", driverRating = 5.0, car = "Auto",
        stops = emptyList(), status = RouteStatus.ACTIVE, ownedByMe = false,
        alternative = false, tag = null, days = "Lunes", recurring = false
    )

    @Test fun prioritizesZoneAndOnTimeArrival() {
        val ranked = Recommender.rank(
            listOf(route(1, "Tlahuelilpan", "6:50 AM"), route(2, "Coyote", "6:45 AM")),
            Session(zone = "Coyote", entryTime = "7:00 AM")
        )
        assertEquals(2L, ranked.first().id)
        assertEquals("Llega a tiempo", ranked.first().tag)
    }
}
