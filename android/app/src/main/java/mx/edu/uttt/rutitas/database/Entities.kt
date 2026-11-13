package mx.edu.uttt.rutitas.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val origin: String,
    val destination: String,
    val departure: String,
    val arrival: String,
    val price: Int,
    val seats: Int,
    val driverName: String,
    val driverRating: Double,
    val car: String,
    val stops: String, // separadas por "|"
    val status: String,
    val ownedByMe: Boolean,
    val alternative: Boolean,
    val tag: String?,
    val days: String,
    val recurring: Boolean,
    val waitMinutes: Int = 5,
    val progress: Int = 0,
    val waitStartedAt: Long = 0L,
    val ownerUserId: Long? = null // null = legado/DEMO sin propietario verificable
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val routeId: Long,
    val origin: String,
    val destination: String,
    val departure: String,
    val pickup: String,
    val driverName: String,
    val car: String,
    val seats: Int,
    val total: Int,
    val status: String,
    val progress: Int,
    val rated: Boolean,
    val seeded: Boolean,
    val dateLabel: String,
    val shift: String = "MATUTINO",
    val direction: String = "ENTRADA",
    val ownerUserId: Long? = null
)


@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passengerName: String,
    val rating: Double,
    val pickup: String,
    val seats: Int,
    val status: String,
    val routeId: Long = 0,
    val shift: String = "MATUTINO",
    val direction: String = "ENTRADA",
    val code: String = "",
    val validated: Boolean = false,
    val passengerType: String = "Alumno",
    val ownerUserId: Long? = null
)


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val type: String,
    val zone: String?,
    val shift: String,
    val entry: String,
    val exit: String,
    val privacy: Boolean
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val refKey: String,
    val role: String,
    val fromName: String,
    val toName: String,
    val stars: Int,
    val tags: String, // separadas por "|"
    val comment: String,
    val routeTitle: String,
    val dateLabel: String,
    val ownerUserId: Long? = null
)
