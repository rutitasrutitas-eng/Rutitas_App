package mx.edu.uttt.rutitas.network

import mx.edu.uttt.rutitas.database.RouteEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Contrato del backend futuro. En modo DEMO no se invoca (ver AppConfig.DEMO_MODE). */
interface ApiService {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @GET("routes") suspend fun routes(): List<RouteDto>
    @POST("bookings") suspend fun createBooking(@Body body: BookingRequestDto): BookingDto
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val name: String, val email: String)

data class RouteDto(
    val id: Long, val origin: String, val destination: String, val departure: String, val arrival: String,
    val price: Int, val seats: Int, val driverName: String, val driverRating: Double, val car: String,
    val stops: List<String>, val days: String = "Lun, Mar, Mié, Jue, Vie", val recurring: Boolean = true
) {
    fun toEntity() = RouteEntity(
        id = id, origin = origin, destination = destination, departure = departure, arrival = arrival,
        price = price, seats = seats, driverName = driverName, driverRating = driverRating, car = car,
        stops = stops.joinToString("|"), status = "ACTIVE", ownedByMe = false, alternative = false,
        tag = null, days = days, recurring = recurring
    )
}

data class BookingRequestDto(val routeId: Long, val pickup: String, val seats: Int)
data class BookingDto(val id: Long, val code: String, val status: String)
