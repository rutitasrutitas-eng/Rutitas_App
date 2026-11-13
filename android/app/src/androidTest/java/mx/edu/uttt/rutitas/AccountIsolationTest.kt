package mx.edu.uttt.rutitas

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.database.BookingEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AccountIsolationTest {
    private lateinit var db: AppDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
    }
    @After fun tearDown() { db.close() }

    private fun booking(owner: Long, code: String) = BookingEntity(
        code = code, routeId = 1, origin = "A", destination = "B", departure = "8:00 AM",
        pickup = "A", driverName = "Conductor", car = "Auto", seats = 1, total = 20,
        status = "CONFIRMED", progress = 0, rated = false, seeded = false, dateLabel = "Hoy",
        ownerUserId = owner
    )

    @Test fun deletingARecordsKeepsBAndLegacy() = runBlocking {
        val a = db.bookingDao().insert(booking(101, "A-1"))
        val b = db.bookingDao().insert(booking(102, "B-1"))
        val legacy = db.bookingDao().insert(booking(101, "OLD").copy(ownerUserId = null))
        db.withTransaction { db.bookingDao().deleteForOwner(101) }
        assertNull(db.bookingDao().get(a))
        assertNotNull(db.bookingDao().get(b))
        assertNotNull(db.bookingDao().get(legacy))
        db.withTransaction { db.bookingDao().deleteForOwner(101) }
        assertNotNull(db.bookingDao().get(b))
    }

    @Test fun deletingUserPhotosKeepsOtherUsersPhotos() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dir = File(context.filesDir, "private_photos").apply { mkdirs() }
        val a = File(dir, "user_101_identity.jpg").apply { writeText("A") }
        val b = File(dir, "user_102_identity.jpg").apply { writeText("B") }
        try {
            val store = PrivatePhotoStore(context)
            store.deleteForUser(101)
            assertFalse(a.exists())
            assertTrue(b.exists())
            store.deleteForUser(101)
            assertTrue(b.exists())
        } finally { a.delete(); b.delete() }
    }
}
