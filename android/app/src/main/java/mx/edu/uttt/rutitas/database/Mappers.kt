package mx.edu.uttt.rutitas.database

import mx.edu.uttt.rutitas.model.*

fun RouteEntity.toDomain() = Route(
    id, origin, destination, departure, arrival, price, seats, driverName, driverRating, car,
    stops.split("|"), RouteStatus.valueOf(status), ownedByMe, alternative, tag, days, recurring, waitMinutes, progress, waitStartedAt
)

fun BookingEntity.toDomain() = Booking(
    id, code, routeId, origin, destination, departure, pickup, driverName, car, seats, total,
    BookingStatus.valueOf(status), progress, rated, dateLabel,
    Shift.valueOf(shift), TripDirection.valueOf(direction)
)

fun RequestEntity.toDomain() = RideRequest(
    id, passengerName, rating, pickup, seats, RequestStatus.valueOf(status), routeId,
    Shift.valueOf(shift), TripDirection.valueOf(direction), code, validated, passengerType
)

fun UserEntity.toProfile() = UserProfile(
    id, name, email, UserType.valueOf(type),
    mx.edu.uttt.rutitas.utils.AppConfig.institutionFor(email) ?: "Institución participante"
)

fun ReviewEntity.toDomain() = Review(
    id, refKey, ReviewRole.valueOf(role), fromName, toName, stars,
    tags.split("|").filter { it.isNotBlank() }, comment, routeTitle, dateLabel
)
