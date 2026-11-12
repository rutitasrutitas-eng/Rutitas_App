package mx.edu.uttt.rutitas.model

enum class UserType(val label: String) { ALUMNO("Alumno"), DOCENTE("Docente"), PERSONAL("Personal") }

enum class Shift(val label: String, val range: String, val entry: String, val exit: String) {
    MATUTINO("Matutino", "7:00 AM – 2:00 PM", "7:00 AM", "2:00 PM"),
    VESPERTINO("Vespertino", "2:00 PM – 9:00 PM", "2:00 PM", "9:00 PM"),
    MIXTO("Mixto", "Horario variable", "8:00 AM", "4:00 PM")
}

enum class RouteStatus(val label: String) {
    ACTIVE("Activa"), IN_PROGRESS("En curso"), INTERRUPTED("Interrumpida"), CANCELLED("Cancelada"), FINISHED("Finalizada")
}
enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmado"), PENDING("Pendiente"), FINISHED("Finalizado"), CANCELLED("Cancelado")
}
enum class RequestStatus(val label: String) {
    PENDING("Pendiente"), ACCEPTED("Aceptada"), REJECTED("Rechazada"), CANCELLED("Cancelada")
}

/** Entrada = voy a la escuela, Salida = regreso a casa. */
enum class TripDirection(val label: String, val hint: String) {
    ENTRADA("Entrada", "Voy a la escuela"), SALIDA("Salida", "Regreso a casa")
}
enum class DocKind(val label: String) {
    LICENCIA("Licencia de conducir"), SEGURO("Seguro del vehículo"), TARJETA("Tarjeta de circulación")
}
enum class DocStatus(val label: String) { PENDING("Pendiente"), COMPLETED("Completado"), REVIEW("Revisar") }
enum class DriverStatus { NONE, IN_PROGRESS, IN_REVIEW, APPROVED }
enum class AccountStatus { ACTIVE, DISABLED }
enum class IncidentType(val label: String) {
    BREAKDOWN("Vehículo descompuesto"), FLAT_TIRE("Llanta ponchada"), ACCIDENT("Accidente"),
    DELAY("Retraso"), OTHER("Otro")
}

enum class ReviewRole { DRIVER_TO_PASSENGER, PASSENGER_TO_DRIVER }

data class Review(
    val id: Long,
    val refKey: String,
    val role: ReviewRole,
    val fromName: String,
    val toName: String,
    val stars: Int,
    val tags: List<String>,
    val comment: String,
    val routeTitle: String,
    val dateLabel: String
)

data class UserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val type: UserType,
    val university: String = "Universidad Tecnológica de Tula-Tepeji"
)

data class Vehicle(
    val brand: String = "", val model: String = "", val year: String = "",
    val color: String = "", val plates: String = "", val seats: Int = 3
) {
    val description: String get() = listOf(brand, model, color).filter { it.isNotBlank() }.joinToString(" ")
}

data class Session(
    val user: UserProfile? = null,
    val zone: String? = null,
    val shift: Shift = Shift.MATUTINO,
    val entryTime: String = "7:00 AM",
    val exitTime: String = "2:00 PM",
    val privacyAccepted: Boolean = false,
    val driverStatus: DriverStatus = DriverStatus.NONE,
    val vehicle: Vehicle = Vehicle(),
    val documents: Map<DocKind, DocStatus> = DocKind.entries.associateWith { DocStatus.PENDING },
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val disabledReason: String? = null,
    val reviewRequested: Boolean = false,
    val profilePhoto: java.io.File? = null
) {
    val isDisabled: Boolean get() = accountStatus == AccountStatus.DISABLED
}

data class Route(
    val id: Long,
    val origin: String,
    val destination: String,
    val departure: String,
    val arrival: String,
    val price: Int,
    val seats: Int,
    val driverName: String,
    val driverRating: Double,
    val car: String,
    val stops: List<String>,
    val status: RouteStatus,
    val ownedByMe: Boolean,
    val alternative: Boolean,
    val tag: String?,
    val days: String,
    val recurring: Boolean,
    val waitMinutes: Int = 5,
    val progress: Int = 0,
    val waitStartedAt: Long = 0L
) {
    val title: String get() = "$origin → $destination"
    val isBookable: Boolean get() = status == RouteStatus.ACTIVE && seats > 0
    val isEditable: Boolean get() = status == RouteStatus.ACTIVE || status == RouteStatus.INTERRUPTED
    val isOpen: Boolean get() = status == RouteStatus.ACTIVE || status == RouteStatus.IN_PROGRESS || status == RouteStatus.INTERRUPTED

    /** Etapas del viaje en curso (incluye Atitalaquia como en la maqueta). */
    fun tripStages(): List<String> =
        if (stops.any { it.contains("Atitalaquia", true) }) stops
        else stops.dropLast(1) + "Atitalaquia" + stops.last()
}

data class Booking(
    val id: Long,
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
    val status: BookingStatus,
    val progress: Int,
    val rated: Boolean,
    val dateLabel: String,
    val shift: Shift = Shift.MATUTINO,
    val direction: TripDirection = TripDirection.ENTRADA
) {
    val title: String get() = "$origin → $destination"
}

data class RideRequest(
    val id: Long,
    val passengerName: String,
    val rating: Double,
    val pickup: String,
    val seats: Int,
    val status: RequestStatus,
    val routeId: Long,
    val shift: Shift,
    val direction: TripDirection,
    val code: String,
    val validated: Boolean,
    val passengerType: String
)

data class Earning(val concept: String, val amount: Int, val date: String)

data class EarningsState(val month: Int, val trips: Int, val movements: List<Earning>) {
    val average: Int get() = if (trips == 0) 0 else month / trips
}

data class Incident(
    val id: Long,
    val routeTitle: String,
    val type: IncidentType,
    val notified: Int,
    val whenLabel: String
)
