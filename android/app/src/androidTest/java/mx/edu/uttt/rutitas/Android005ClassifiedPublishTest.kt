package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
class Android005ClassifiedPublishTest {

    private fun login(
        session: SessionManager,
        status: DriverStatus
    ) {
        session.update {
            Session(
                user = UserProfile(
                    101L,
                    "Conductor Prueba",
                    "101@uttt.edu.mx",
                    UserType.ALUMNO
                ),
                driverStatus = status
            )
        }
    }

    private fun repository(
        db: AppDatabase,
        session: SessionManager
    ): RouteRepository {
        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .build()
            .create(ApiService::class.java)

        return RouteRepository(
            db.routeDao(), api, session, db.bookingDao()
        )
    }

    private suspend fun publish(
        repo: RouteRepository,
        seats: Int = 3
    ) = repo.publishClassified(
        origin = "Coyote",
        destination = "UTTT",
        departure = "7:00 AM",
        seats = seats,
        price = 20,
        days = "Lun, Mar",
        recurring = false,
        waitMinutes = 5,
        shift = Shift.MATUTINO,
        direction = TripDirection.ENTRADA
    )

    @Test
    fun approvedDriverPublishesClassifiedRoute() = runBlocking {
        val ctx =
            ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(
            ctx, AppDatabase::class.java
        ).build()

        try {
            val session = SessionManager()
            login(session, DriverStatus.APPROVED)
            val repo = repository(db, session)

            val result = publish(repo)

            assertTrue(result.toString(), result.isSuccess)

            val route = db.routeDao().get(result.getOrThrow())!!
            assertEquals(101L, route.ownerUserId)
            assertEquals("MATUTINO", route.shift)
            assertEquals("ENTRADA", route.direction)
            assertEquals("7:00 AM", route.departure)
            assertEquals(3, route.seats)
            assertEquals(20, route.price)
        } finally {
            db.close()
        }
    }

    @Test
    fun unapprovedUserCannotPublish() = runBlocking {
        val ctx =
            ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(
            ctx, AppDatabase::class.java
        ).build()

        try {
            val session = SessionManager()
            login(session, DriverStatus.NONE)
            val repo = repository(db, session)

            assertTrue(publish(repo).isFailure)
            assertTrue(db.routeDao().observeAll().first().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun invalidSeatsCannotBePublished() = runBlocking {
        val ctx =
            ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(
            ctx, AppDatabase::class.java
        ).build()

        try {
            val session = SessionManager()
            login(session, DriverStatus.APPROVED)
            val repo = repository(db, session)

            assertTrue(publish(repo, seats = 0).isFailure)
            assertTrue(db.routeDao().observeAll().first().isEmpty())
        } finally {
            db.close()
        }
    }
}