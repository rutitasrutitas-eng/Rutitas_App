package mx.edu.uttt.rutitas.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.repository.*
import java.io.File
import javax.inject.Inject

data class MapUiState(
    val loading: Boolean = true,
    val userName: String? = null,
    val zone: String? = null,
    val routes: List<Route> = emptyList(),
    val accountDisabled: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    routeRepo: RouteRepository,
    session: SessionManager,
    seeder: DemoSeeder
) : ViewModel() {
    val state: StateFlow<MapUiState> = combine(routeRepo.publicRoutes, session.state, seeder.ready) { routes, s, ready ->
        MapUiState(!ready, s.user?.name, s.zone, routes, s.isDisabled)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())
}

data class AuthUiState(val loading: Boolean = false, val error: String? = null)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
    sessionManager: SessionManager
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()
    val session: StateFlow<Session> = sessionManager.state

    fun clearError() = _ui.update { it.copy(error = null) }

    fun login(email: String, pass: String, onDone: (disabled: Boolean) -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            auth.login(email, pass)
                .onSuccess { _ui.value = AuthUiState(); onDone(it) }
                .onFailure { _ui.value = AuthUiState(error = it.message) }
        }
    }

    fun register(name: String, email: String, pass: String, type: UserType, onDone: () -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            auth.register(name, email, pass, type)
                .onSuccess { _ui.value = AuthUiState(); onDone() }
                .onFailure { _ui.value = AuthUiState(error = it.message) }
        }
    }

    fun restoreDemo() {
        auth.restoreDemoAccounts()
        _ui.value = AuthUiState()
    }

    fun setZone(zone: String) { viewModelScope.launch { auth.savePrefs(zone = zone) } }
    fun saveSchedule(shift: Shift, entry: String, exit: String) {
        viewModelScope.launch { auth.savePrefs(shift = shift, entry = entry, exit = exit) }
    }
    fun acceptPrivacy() { viewModelScope.launch { auth.savePrefs(privacy = true) } }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookingViewModel @Inject constructor(
    handle: SavedStateHandle,
    routeRepo: RouteRepository,
    private val bookingRepo: BookingRepository,
    private val reviewRepo: ReviewRepository,
    sessionManager: SessionManager
) : ViewModel() {
    private val routeId: Long = handle.get<Long>("routeId") ?: -1L
    private val bookingId: Long = handle.get<Long>("bookingId") ?: -1L

    val session: StateFlow<Session> = sessionManager.state

    val booking: StateFlow<Booking?> = bookingRepo.observe(bookingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Reseñas que los pasajeros le han dado al conductor de esta ruta (se muestran antes de reservar). */
    val driverReviews: StateFlow<List<Review>> by lazy {
        combine(route, reviewRepo.reviews) { r, rs ->
            rs.filter { it.role == ReviewRole.PASSENGER_TO_DRIVER && it.toName == r?.driverName }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val route: StateFlow<Route?> = (
        if (routeId >= 0) routeRepo.observe(routeId)
        else bookingRepo.observe(bookingId).flatMapLatest { b ->
            if (b == null) flowOf(null) else routeRepo.observe(b.routeId)
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun reserve(
        pickup: String, seats: Int, shift: Shift, direction: TripDirection,
        onDone: (Long) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            bookingRepo.reserve(routeId, pickup, seats, shift, direction)
                .onSuccess { _loading.value = false; onDone(it) }
                .onFailure { _loading.value = false; onError(it.message ?: "No se pudo reservar.") }
        }
    }

    fun startTrip() { viewModelScope.launch { bookingRepo.startTrip(bookingId) } }
    fun advance(stageCount: Int) { viewModelScope.launch { bookingRepo.advance(bookingId, stageCount) } }
    /** Guarda la calificación del pasajero al conductor en el historial de reseñas. */
    fun rate(stars: Int, tags: Set<String>, comment: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val b = booking.value
            val me = session.value.user?.name ?: "Pasajero"
            if (b != null) {
                reviewRepo.add("booking:${b.id}", ReviewRole.PASSENGER_TO_DRIVER, me, b.driverName, stars, tags, comment, b.title)
            }
            bookingRepo.rate(bookingId)
            onDone()
        }
    }
}

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repo: BookingRepository,
    sessionManager: SessionManager
) : ViewModel() {
    val session: StateFlow<Session> = sessionManager.state
    val bookings: StateFlow<List<Booking>> = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val interrupted: StateFlow<List<Booking>> = repo.interrupted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancel(id: Long) { viewModelScope.launch { repo.cancel(id) } }
    fun confirm(id: Long) { viewModelScope.launch { repo.confirmPending(id) } }
}

fun averageStars(list: List<Review>): Double =
    if (list.isEmpty()) 0.0 else Math.round(list.sumOf { it.stars } * 10.0 / list.size) / 10.0

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val seeder: DemoSeeder,
    private val sm: SessionManager,
    reviewRepo: ReviewRepository,
    bookingRepo: BookingRepository
) : ViewModel() {
    val session: StateFlow<Session> = sm.state

    /** Promedio y cantidad de reseñas recibidas como pasajero (calificaciones de conductores). */
    val passengerStats: StateFlow<Pair<Double, Int>> = combine(reviewRepo.reviews, sm.state) { rs, s ->
        val got = rs.filter { it.role == ReviewRole.DRIVER_TO_PASSENGER && it.toName == s.user?.name }
        averageStars(got) to got.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0 to 0)

    val finishedTrips: StateFlow<Int> = bookingRepo.bookings
        .map { l -> l.count { it.status == BookingStatus.FINISHED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logout() = auth.logout()
    fun deleteAccount(onDone: () -> Unit) { viewModelScope.launch { auth.deleteAccount(); onDone() } }
    fun disable(reason: String) = sm.update { it.copy(accountStatus = AccountStatus.DISABLED, disabledReason = reason, reviewRequested = false) }
    fun enable() = sm.update { it.copy(accountStatus = AccountStatus.ACTIVE, disabledReason = null, reviewRequested = false) }
    fun requestReview() = sm.update { it.copy(reviewRequested = true) }
    fun resetDemo() = seeder.reset()

    fun setPhoto(source: Any) { viewModelScope.launch { auth.setProfilePhoto(source) } }
    fun removePhoto() = auth.removeProfilePhoto()

    fun updateProfile(
        name: String, email: String, type: UserType, zone: String?, shift: Shift, entry: String, exit: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch { onResult(auth.updateProfile(name, email, type, zone, shift, entry, exit)) }
    }
}

data class ReviewsUiState(
    val asDriver: Boolean = false,
    val received: List<Review> = emptyList(),
    val sent: List<Review> = emptyList(),
    val average: Double = 0.0
)

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    handle: SavedStateHandle,
    reviewRepo: ReviewRepository,
    sessionManager: SessionManager
) : ViewModel() {
    private val asDriver: Boolean = handle.get<Boolean>("driver") ?: false

    val state: StateFlow<ReviewsUiState> = combine(reviewRepo.reviews, sessionManager.state) { rs, s ->
        val me = s.user?.name.orEmpty()
        val receivedRole = if (asDriver) ReviewRole.PASSENGER_TO_DRIVER else ReviewRole.DRIVER_TO_PASSENGER
        val sentRole = if (asDriver) ReviewRole.DRIVER_TO_PASSENGER else ReviewRole.PASSENGER_TO_DRIVER
        val received = rs.filter { it.role == receivedRole && it.toName == me }
        val sent = rs.filter { it.role == sentRole && it.fromName == me }
        ReviewsUiState(asDriver, received, sent, averageStars(received))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReviewsUiState(asDriver))
}

@HiltViewModel
class DriverOnboardingViewModel @Inject constructor(
    private val repo: DriverRepository,
    sessionManager: SessionManager
) : ViewModel() {
    val session: StateFlow<Session> = sessionManager.state
    val photos: StateFlow<Map<String, File>> = repo.photos.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    fun start() = repo.startOnboarding()
    fun setPhoto(key: String, photo: Any) { viewModelScope.launch { repo.setPhoto(key, photo) } }
    fun removePhoto(key: String) = repo.removePhoto(key)
    fun saveVehicle(v: Vehicle) = repo.saveVehicle(v)
    fun setDoc(kind: DocKind, photo: Any?) { viewModelScope.launch { repo.setDoc(kind, photo) } }
    fun completeAllDocs() = repo.completeAllDocs()
    fun submit() = repo.submitForReview()
    fun approve() { viewModelScope.launch { repo.approve() } }
}

@HiltViewModel
class DriverViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val routeRepo: RouteRepository,
    private val repo: DriverRepository,
    private val reviewRepo: ReviewRepository,
    sessionManager: SessionManager
) : ViewModel() {
    /** Ruta abierta en la pantalla actual (detalle o edición); -1 si no aplica. */
    private val routeId: Long = handle.get<Long>("routeId") ?: -1L
    val editing: Boolean get() = routeId >= 0

    val session: StateFlow<Session> = sessionManager.state
    val myRoutes: StateFlow<List<Route>> = routeRepo.myRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val route: StateFlow<Route?> = routeRepo.observe(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val requests: StateFlow<List<RideRequest>> = repo.requests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    /** Solicitudes de la ruta abierta: las aceptadas en ella y las pendientes por atender. */
    val routeRequests: StateFlow<List<RideRequest>> = repo.requests
        .map { l -> l.filter { it.routeId == routeId || (it.routeId == 0L && it.status == RequestStatus.PENDING) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val earnings: StateFlow<EarningsState> = repo.earnings
    val incidents: StateFlow<List<Incident>> = repo.incidents

    /** Calificación promedio y total de reseñas recibidas como conductor. */
    val driverStats: StateFlow<Pair<Double, Int>> = combine(reviewRepo.reviews, sessionManager.state) { rs, s ->
        val got = rs.filter { it.role == ReviewRole.PASSENGER_TO_DRIVER && it.toName == s.user?.name }
        averageStars(got) to got.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0 to 0)

    /** Referencias de reseñas ya enviadas (para no calificar dos veces al mismo pasajero). */
    val reviewedKeys: StateFlow<Set<String>> = reviewRepo.reviews.map { l -> l.map { it.refKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun rateRequest(q: RideRequest, stars: Int, tags: Set<String>, comment: String) {
        viewModelScope.launch {
            val me = session.value.user?.name ?: "Conductor"
            val title = route.value?.title ?: "Ruta"
            reviewRepo.add("route:$routeId:req:${q.id}", ReviewRole.DRIVER_TO_PASSENGER, me, q.passengerName, stars, tags, comment, title)
        }
    }

    fun startWait() { viewModelScope.launch { repo.startWait(routeId) } }
    fun endWait() { viewModelScope.launch { repo.endWait(routeId) } }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    suspend fun accept(id: Long, forRoute: Long? = null): String? = repo.acceptRequest(id, forRoute)
    fun reject(id: Long) { viewModelScope.launch { repo.rejectRequest(id) } }
    fun resume(id: Long) { viewModelScope.launch { routeRepo.setStatus(id, RouteStatus.ACTIVE) } }

    fun startRoute(id: Long, onResult: (String?) -> Unit) { viewModelScope.launch { onResult(repo.startRoute(id)) } }
    fun advance(stageCount: Int) { viewModelScope.launch { repo.advanceRoute(routeId, stageCount) } }
    fun finish() { viewModelScope.launch { repo.finishRoute(routeId) } }
    fun cancelRoute(onDone: () -> Unit) { viewModelScope.launch { repo.cancelRoute(routeId); onDone() } }
    fun validate(code: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch { onResult(repo.validateCode(routeId, code)) }
    }

    /** Publica una ruta nueva o, si se abrió en modo edición, guarda los cambios. */
    fun save(
        origin: String, destination: String, time: String, seats: Int, price: Int, days: String,
        recurring: Boolean, waitMinutes: Int, stops: List<String>?, onDone: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            val result = if (editing) {
                routeRepo.update(routeId, origin, destination, time, seats, price, days, recurring, waitMinutes, stops)
            } else {
                routeRepo.publish(origin, destination, time, seats, price, days, recurring, waitMinutes, stops).map { }
            }
            _loading.value = false
            result.onSuccess { onDone() }.onFailure { onError(it.message ?: "No se pudo guardar la ruta.") }
        }
    }
}

@HiltViewModel
class IncidentViewModel @Inject constructor(
    handle: SavedStateHandle,
    routeRepo: RouteRepository,
    private val repo: IncidentRepository
) : ViewModel() {
    private val routeId: Long = handle.get<Long>("routeId") ?: -1L

    val route: StateFlow<Route?> = routeRepo.observe(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val alternatives: StateFlow<List<Route>> = routeRepo.alternatives(routeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val latest: StateFlow<Incident?> = repo.incidents.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun report(type: IncidentType, onDone: () -> Unit) {
        viewModelScope.launch { repo.report(routeId, type); onDone() }
    }
}

data class DriverReviewsUiState(val name: String = "", val reviews: List<Review> = emptyList(), val average: Double = 0.0)

/** Reseñas públicas de un conductor, visibles para los pasajeros. */
@HiltViewModel
class DriverPublicReviewsViewModel @Inject constructor(
    handle: SavedStateHandle,
    reviewRepo: ReviewRepository
) : ViewModel() {
    private val name: String = handle.get<String>("name") ?: ""

    val state: StateFlow<DriverReviewsUiState> = reviewRepo.reviews.map { rs ->
        val list = rs.filter { it.role == ReviewRole.PASSENGER_TO_DRIVER && it.toName == name }
        DriverReviewsUiState(name, list, averageStars(list))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DriverReviewsUiState(name))
}
