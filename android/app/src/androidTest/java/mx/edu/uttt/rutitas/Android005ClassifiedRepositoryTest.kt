package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.network.ApiService
import mx.edu.uttt.rutitas.repository.RouteRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit

@RunWith(AndroidJUnit4::class)
class Android005ClassifiedRepositoryTest {

    private fun login(
        session: SessionManager,
        id: Long,
        status: DriverStatus
    ) {
        session.update {
            Session(
                user = UserProfile(
                    id,
                    "Usuario$id",
                    "$id@uttt.edu.mx",
                    UserType.ALUMNO
                ),
                driverStatus = status
            )
        }
    }

    private suspend fun save(
        repository: RouteRepository,
        routeId: Long
    ) = repository.updateClassified(
        id = routeId,
        origin = "Tula Centro",
        destination = "UTTT",
        departure = "8:00 AM",
        seats = 3,
        price = 25,
        days = "Lun, Mar",
        recurring = false,
        waitMinutes = 5,
        shift = Shift.MATUTINO,
        direction = TripDirection.ENTRADA
    )

    @Test
    fun approvedOwnerCanEditClassifiedRoute() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()

        try {
            val session = SessionManager()
            val api = Retrofit.Builder()
                .baseUrl("https://example.invalid/")
                .build()
                .create(ApiService::class.java)

            val repository = RouteRepository(
                db.routeDao(), api, session, db.bookingDao()
            )

            val routeId = 86001L
            db.routeDao().insert(
                DemoData.routes().first().copy(
                    id = routeId,
                    ownerUserId = 101L,
                    origin = "Coyote",
                    destination = "UTTT",
                    seats = 3,
                    status = "ACTIVE",
                    shift = null,
                    direction = null
                )
            )

            login(session, 101L, DriverStatus.APPROVED)

            assertTrue(save(repository, routeId).isSuccess)

            val saved = db.routeDao().get(routeId)!!
            assertEquals("Tula Centro", saved.origin)
            assertEquals(25, saved.price)
            assertEquals("MATUTINO", saved.shift)
            assertEquals("ENTRADA", saved.direction)
        } finally {
            db.close()
        }
    }

    @Test
    fun foreignDriverCannotEditClassifiedRoute() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()

        try {
            val session = SessionManager()
            val api = Retrofit.Builder()
                .baseUrl("https://example.invalid/")
                .build()
                .create(ApiService::class.java)

            val repository = RouteRepository(
                db.routeDao(), api, session, db.bookingDao()
            )

            val routeId = 86002L
            db.routeDao().insert(
                DemoData.routes().first().copy(
                    id = routeId,
                    ownerUserId = 101L,
                    origin = "Coyote",
                    seats = 3,
                    status = "ACTIVE",
                    shift = null,
                    direction = null
                )
            )

            login(session, 202L, DriverStatus.APPROVED)

            assertTrue(save(repository, routeId).isFailure)
            assertEquals("Coyote", db.routeDao().get(routeId)?.origin)
            assertNull(db.routeDao().get(routeId)?.shift)
        } finally {
            db.close()
        }
    }

    @Test
    fun unapprovedUserCannotEditClassifiedRoute() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()

        try {
            val session = SessionManager()
            val api = Retrofit.Builder()
                .baseUrl("https://example.invalid/")
                .build()
                .create(ApiService::class.java)

            val repository = RouteRepository(
                db.routeDao(), api, session, db.bookingDao()
            )

            val routeId = 86003L
            db.routeDao().insert(
                DemoData.routes().first().copy(
                    id = routeId,
                    ownerUserId = 101L,
                    origin = "Coyote",
                    seats = 3,
                    status = "ACTIVE",
                    shift = null,
                    direction = null
                )
            )

            login(session, 101L, DriverStatus.NONE)

            assertTrue(save(repository, routeId).isFailure)
            assertEquals("Coyote", db.routeDao().get(routeId)?.origin)
            assertNull(db.routeDao().get(routeId)?.shift)
        } finally {
            db.close()
        }
    }
}