package mx.edu.uttt.rutitas.network

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
    val phone: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: BackendUserDto
)

data class BackendUserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val phone: String?,
    val role: String,
    val createdAt: String
)

data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val phone: String? = null
)

data class BackendLocationDto(
    val name: String,
    val lat: Double,
    val lng: Double
)

data class BackendStopDto(
    val order: Int,
    val name: String,
    val lat: Double,
    val lng: Double
)

data class BackendRouteDto(
    val id: String,
    val code: String,
    val name: String,
    val origin: BackendLocationDto,
    val destination: BackendLocationDto,
    val distanceKm: Double,
    val estimatedDurationMin: Int
)

data class BackendRouteDetailDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val origin: BackendLocationDto,
    val destination: BackendLocationDto,
    val stops: List<BackendStopDto>,
    val distanceKm: Double,
    val estimatedDurationMin: Int
)

data class PagedRoutesDto(
    val items: List<BackendRouteDto>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)

data class BackendProblemDto(
    val type: String?,
    val title: String?,
    val status: Int?,
    val detail: String?,
    val traceId: String?,
    val errors: Map<String, List<String>>? = null
)
