package mx.edu.uttt.rutitas.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.database.ReviewDao
import mx.edu.uttt.rutitas.database.RequestDao
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.model.AccountStatus
import mx.edu.uttt.rutitas.model.DocKind
import mx.edu.uttt.rutitas.model.DocStatus
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.model.Vehicle
import mx.edu.uttt.rutitas.repository.DriverRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Carga los datos DEMO en Room la primera vez que se abre la app. */
@Singleton
class DemoSeeder @Inject constructor(
    private val routeDao: RouteDao,
    private val bookingDao: BookingDao,
    private val requestDao: RequestDao,
    private val reviewDao: ReviewDao,
    private val driverRepo: DriverRepository,
    private val session: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun seedIfNeeded() {
        scope.launch {
            if (routeDao.count() == 0) insertAll()
            _ready.value = true
        }
    }

    /** Restablece solo el estado DEMO en memoria; no borra datos de otros usuarios. */
    fun reset() {
        scope.launch {
            _ready.value = false
            // Únicamente si la base está vacía se agregan registros de ejemplo.
            // No se reemplazan registros existentes (incluidos los heredados).
            if (routeDao.count() == 0) insertAll()
            session.update {
                it.copy(
                    driverStatus = DriverStatus.NONE, vehicle = Vehicle(),
                    documents = DocKind.entries.associateWith { DocStatus.PENDING },
                    accountStatus = AccountStatus.ACTIVE, disabledReason = null, reviewRequested = false
                )
            }
            _ready.value = true
        }
    }

    private suspend fun insertAll() {
        routeDao.insertDemoIfAbsent(DemoData.routes())
        bookingDao.insertDemoIfAbsent(DemoData.bookings())
        requestDao.insertDemoIfAbsent(DemoData.requests())
        reviewDao.insertDemoIfAbsent(DemoData.reviews())
    }
}
