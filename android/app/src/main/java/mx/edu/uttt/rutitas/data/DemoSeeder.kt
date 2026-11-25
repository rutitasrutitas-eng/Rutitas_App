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
            try {
                // IGNORE en las cuatro tablas conserva datos existentes y repone faltantes.
                if (routeDao.count() == 0) insertAll()
                _ready.value = true
            } catch (e: Exception) {
                _ready.value = false
                throw e
            }
        }
    }

    /** Espera el resultado antes de avisar a la UI; no borra datos de ningun usuario. */
    suspend fun reset() {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            _ready.value = false
            try {
                insertAll()
                session.update { current ->
                    if (current.user?.email == DemoData.DEMO_EMAIL)
                        current.copy(driverStatus = DriverStatus.NONE, vehicle = Vehicle(),
                            documents = DocKind.entries.associateWith { DocStatus.PENDING },
                            accountStatus = AccountStatus.ACTIVE, disabledReason = null,
                            reviewRequested = false)
                    else current
                }
                _ready.value = true
            } catch (e: Exception) {
                _ready.value = false
                throw e
            }
        }
    }

    private suspend fun insertAll() {
        routeDao.insertDemoIfAbsent(DemoData.routes())
        bookingDao.insertDemoIfAbsent(DemoData.bookings())
        requestDao.insertDemoIfAbsent(DemoData.requests())
        reviewDao.insertDemoIfAbsent(DemoData.reviews())
    }
}
