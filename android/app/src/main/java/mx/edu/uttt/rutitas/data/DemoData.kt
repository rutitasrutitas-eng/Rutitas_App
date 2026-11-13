package mx.edu.uttt.rutitas.data

import mx.edu.uttt.rutitas.database.BookingEntity
import mx.edu.uttt.rutitas.database.RequestEntity
import mx.edu.uttt.rutitas.database.ReviewEntity
import mx.edu.uttt.rutitas.database.RouteEntity
import mx.edu.uttt.rutitas.model.*

object DemoData {
    const val DEMO_EMAIL = "demo@rutitas.com"
    const val DEMO_PASSWORD = "123456"
    const val BLOCKED_EMAIL = "bloqueado@rutitas.com"

    val demoUser = UserProfile(0, "María Torres", DEMO_EMAIL, UserType.ALUMNO)
    val blockedUser = UserProfile(-1, "Jorge Mendoza", BLOCKED_EMAIL, UserType.ALUMNO)

    val zones = listOf("Coyote", "Tlahuelilpan", "El Carmen", "Tula Centro", "Atitalaquia")
    val disabledReasons = listOf("Documento vencido", "Cuenta suspendida", "Verificación pendiente")
    const val WEEK_DAYS = "Lun, Mar, Mié, Jue, Vie"

    fun routes() = listOf(
        RouteEntity(
            id = 1, origin = "Coyote", destination = "UTTT", departure = "6:20 AM", arrival = "6:50 AM",
            price = 20, seats = 2, driverName = "Carlos García", driverRating = 4.8, car = "Nissan Versa gris",
            stops = "Coyote Centro|Plaza Jardines|UTTT", status = "ACTIVE", ownedByMe = false,
            alternative = false, tag = null, days = WEEK_DAYS, recurring = true
        ),
        RouteEntity(
            id = 2, origin = "Tlahuelilpan", destination = "UTTT", departure = "6:30 AM", arrival = "6:55 AM",
            price = 25, seats = 3, driverName = "Ana López", driverRating = 4.9, car = "Chevrolet Aveo blanco",
            stops = "Tlahuelilpan Centro|Plaza Jardines|UTTT", status = "ACTIVE", ownedByMe = false,
            alternative = false, tag = null, days = WEEK_DAYS, recurring = true
        ),
        RouteEntity(
            id = 3, origin = "El Carmen", destination = "UTTT", departure = "6:40 AM", arrival = "7:00 AM",
            price = 20, seats = 1, driverName = "Luis Hernández", driverRating = 4.7, car = "Volkswagen Vento rojo",
            stops = "El Carmen Centro|Atitalaquia|UTTT", status = "ACTIVE", ownedByMe = false,
            alternative = false, tag = null, days = WEEK_DAYS, recurring = true
        ),
        // Ruta alternativa: solo aparece cuando una ruta se interrumpe
        RouteEntity(
            id = 4, origin = "Tlahuelilpan", destination = "UTTT", departure = "6:35 AM", arrival = "6:55 AM",
            price = 20, seats = 2, driverName = "Rodrigo Salinas", driverRating = 4.6, car = "Toyota Yaris azul",
            stops = "Tlahuelilpan Centro|UTTT", status = "ACTIVE", ownedByMe = false,
            alternative = true, tag = "Llega a tiempo", days = WEEK_DAYS, recurring = true
        )
    )

    fun bookings() = listOf(
        BookingEntity(
            id = 1, code = "RT-2390", routeId = 2, origin = "Tlahuelilpan", destination = "UTTT",
            departure = "6:30 AM", pickup = "Tlahuelilpan Centro", driverName = "Ana López",
            car = "Chevrolet Aveo blanco", seats = 1, total = 25, status = "PENDING", progress = 0,
            rated = false, seeded = true, dateLabel = "Mañana"
        ),
        BookingEntity(
            id = 2, code = "RT-2301", routeId = 3, origin = "El Carmen", destination = "UTTT",
            departure = "6:40 AM", pickup = "El Carmen Centro", driverName = "Luis Hernández",
            car = "Volkswagen Vento rojo", seats = 1, total = 20, status = "FINISHED", progress = 4,
            rated = false, seeded = true, dateLabel = "Ayer"
        ),
        BookingEntity(
            id = 3, code = "RT-2288", routeId = 1, origin = "Coyote", destination = "UTTT",
            departure = "6:20 AM", pickup = "Coyote Centro", driverName = "Carlos García",
            car = "Nissan Versa gris", seats = 1, total = 20, status = "CANCELLED", progress = 0,
            rated = false, seeded = true, dateLabel = "Hace 3 días"
        ),
        BookingEntity(
            id = 4, code = "RT-2260", routeId = 2, origin = "Tlahuelilpan", destination = "UTTT",
            departure = "6:30 AM", pickup = "Tlahuelilpan Centro", driverName = "Ana López",
            car = "Chevrolet Aveo blanco", seats = 1, total = 25, status = "FINISHED", progress = 4,
            rated = true, seeded = true, dateLabel = "Hace 5 días"
        )
    )

    fun requests() = listOf(
        RequestEntity(id = 1, passengerName = "Ana López", rating = 4.9, pickup = "Coyote Centro", seats = 1, status = "PENDING",
            shift = "MATUTINO", direction = "ENTRADA", code = "RT-3101", passengerType = "Alumno"),
        RequestEntity(id = 2, passengerName = "Luis Hernández", rating = 4.7, pickup = "Plaza Jardines", seats = 1, status = "PENDING",
            shift = "VESPERTINO", direction = "SALIDA", code = "RT-3102", passengerType = "Docente"),
        RequestEntity(id = 3, passengerName = "Sofía Ramírez", rating = 5.0, pickup = "Coyote Centro", seats = 1, status = "PENDING",
            shift = "MATUTINO", direction = "SALIDA", code = "RT-3103", passengerType = "Alumno")
    )

    fun ownRoute(driverName: String, car: String) = RouteEntity(
        origin = "Coyote", destination = "UTTT", departure = "6:20 AM", arrival = "6:50 AM",
        price = 20, seats = 2, driverName = driverName, driverRating = 4.8, car = car.ifBlank { "Nissan Versa gris" },
        stops = "Coyote Centro|Plaza Jardines|UTTT", status = "ACTIVE", ownedByMe = true,
        alternative = false, tag = null, days = WEEK_DAYS, recurring = true
    )

    fun earnings() = EarningsState(
        month = 1240, trips = 18,
        movements = listOf(
            Earning("Coyote → UTTT", 40, "Hoy"),
            Earning("Coyote → UTTT", 60, "Ayer"),
            Earning("Coyote → UTTT", 20, "Ayer"),
            Earning("Coyote → UTTT", 80, "Lun 12"),
            Earning("Coyote → UTTT", 40, "Vie 9")
        )
    )

    fun incidents() = listOf(
        Incident(-1, "Coyote → UTTT", IncidentType.DELAY, 0, "Hace 2 semanas"),
        Incident(-2, "El Carmen → UTTT", IncidentType.FLAT_TIRE, 1, "Hace 1 mes")
    )

    private const val D2P = "DRIVER_TO_PASSENGER"
    private const val P2D = "PASSENGER_TO_DRIVER"

    /** Reseñas de ejemplo ligadas a la cuenta demo (María Torres) tanto como pasajera como conductora. */
    fun reviews() = listOf(
        // Conductores -> María (como pasajera)
        ReviewEntity(refKey = "seed1", role = D2P, fromName = "Carlos García", toName = "María Torres", stars = 5,
            tags = "Puntual|Amable", comment = "Siempre lista en el punto de encuentro.", routeTitle = "Coyote → UTTT", dateLabel = "Hace 3 días"),
        ReviewEntity(refKey = "seed2", role = D2P, fromName = "Luis Hernández", toName = "María Torres", stars = 4,
            tags = "Buena comunicación", comment = "", routeTitle = "El Carmen → UTTT", dateLabel = "Ayer"),
        // María (como pasajera) -> conductores
        ReviewEntity(refKey = "seed3", role = P2D, fromName = "María Torres", toName = "Carlos García", stars = 5,
            tags = "Manejo seguro|Auto limpio", comment = "Muy buen viaje.", routeTitle = "Coyote → UTTT", dateLabel = "Hace 3 días"),
        ReviewEntity(refKey = "seed4", role = P2D, fromName = "María Torres", toName = "Luis Hernández", stars = 4,
            tags = "Puntual", comment = "", routeTitle = "El Carmen → UTTT", dateLabel = "Ayer"),
        // Pasajeros -> María (como conductora)
        ReviewEntity(refKey = "seed5", role = P2D, fromName = "Ana López", toName = "María Torres", stars = 5,
            tags = "Manejo seguro|Auto limpio", comment = "Muy amable y puntual.", routeTitle = "Coyote → UTTT", dateLabel = "Hace 1 semana"),
        ReviewEntity(refKey = "seed6", role = P2D, fromName = "Sofía Ramírez", toName = "María Torres", stars = 5,
            tags = "Puntual|Buena comunicación", comment = "", routeTitle = "Coyote → UTTT", dateLabel = "Hace 2 semanas"),
        // María (como conductora) -> pasajeros
        ReviewEntity(refKey = "seed7", role = D2P, fromName = "María Torres", toName = "Ana López", stars = 5,
            tags = "Puntual|Amable", comment = "", routeTitle = "Coyote → UTTT", dateLabel = "Hace 1 semana")
    ,
        // Pasajeros -> conductores (lo que ve un pasajero antes de reservar)
        ReviewEntity(refKey = "seed8", role = P2D, fromName = "Sofía Ramírez", toName = "Carlos García", stars = 5,
            tags = "Puntual|Manejo seguro", comment = "Siempre sale a la hora indicada.", routeTitle = "Coyote → UTTT", dateLabel = "Hace 4 días"),
        ReviewEntity(refKey = "seed9", role = P2D, fromName = "Jorge Mendoza", toName = "Carlos García", stars = 4,
            tags = "Auto limpio", comment = "Buen viaje, solo un poco de tráfico.", routeTitle = "Coyote → UTTT", dateLabel = "Hace 1 semana"),
        ReviewEntity(refKey = "seed10", role = P2D, fromName = "Daniela Cruz", toName = "Ana López", stars = 5,
            tags = "Amable|Buena comunicación", comment = "Muy atenta, avisa cuando va a llegar.", routeTitle = "Tlahuelilpan → UTTT", dateLabel = "Hace 2 días"),
        ReviewEntity(refKey = "seed11", role = P2D, fromName = "Sofía Ramírez", toName = "Ana López", stars = 5,
            tags = "Puntual", comment = "", routeTitle = "Tlahuelilpan → UTTT", dateLabel = "Hace 1 semana"),
        ReviewEntity(refKey = "seed12", role = P2D, fromName = "Daniela Cruz", toName = "Luis Hernández", stars = 4,
            tags = "Manejo seguro", comment = "Conduce con cuidado.", routeTitle = "El Carmen → UTTT", dateLabel = "Hace 5 días"),
        ReviewEntity(refKey = "seed13", role = P2D, fromName = "Jorge Mendoza", toName = "Rodrigo Salinas", stars = 5,
            tags = "Puntual|Amable", comment = "Excelente conductor.", routeTitle = "Tlahuelilpan → UTTT", dateLabel = "Hace 3 días")
    )
}
