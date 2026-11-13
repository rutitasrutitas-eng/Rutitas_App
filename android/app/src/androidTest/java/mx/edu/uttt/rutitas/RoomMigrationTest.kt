package mx.edu.uttt.rutitas

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.database.AppDatabase
import mx.edu.uttt.rutitas.database.MIGRATION_3_4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Reproduce el esquema original de las entidades Room v3 sin ownerUserId,
 * migra a v4 mediante Room y comprueba que los registros siguen existiendo.
 * No usa la base de datos real de la aplicación.
 */
@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {
    @Test
    fun migration3To4PreservesExistingRows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "test_migration_v3_to_v4_${System.nanoTime()}.db"
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()

        try {
            SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
                // Esquema reproducido a partir de Entities.kt en HEAD (Room v3).
                old.execSQL("""
                    CREATE TABLE IF NOT EXISTS `routes` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `origin` TEXT NOT NULL, `destination` TEXT NOT NULL,
                      `departure` TEXT NOT NULL, `arrival` TEXT NOT NULL,
                      `price` INTEGER NOT NULL, `seats` INTEGER NOT NULL,
                      `driverName` TEXT NOT NULL, `driverRating` REAL NOT NULL,
                      `car` TEXT NOT NULL, `stops` TEXT NOT NULL,
                      `status` TEXT NOT NULL, `ownedByMe` INTEGER NOT NULL,
                      `alternative` INTEGER NOT NULL, `tag` TEXT,
                      `days` TEXT NOT NULL, `recurring` INTEGER NOT NULL,
                      `waitMinutes` INTEGER NOT NULL, `progress` INTEGER NOT NULL,
                      `waitStartedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                old.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bookings` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `code` TEXT NOT NULL, `routeId` INTEGER NOT NULL,
                      `origin` TEXT NOT NULL, `destination` TEXT NOT NULL,
                      `departure` TEXT NOT NULL, `pickup` TEXT NOT NULL,
                      `driverName` TEXT NOT NULL, `car` TEXT NOT NULL,
                      `seats` INTEGER NOT NULL, `total` INTEGER NOT NULL,
                      `status` TEXT NOT NULL, `progress` INTEGER NOT NULL,
                      `rated` INTEGER NOT NULL, `seeded` INTEGER NOT NULL,
                      `dateLabel` TEXT NOT NULL, `shift` TEXT NOT NULL,
                      `direction` TEXT NOT NULL
                    )
                """.trimIndent())
                old.execSQL("""
                    CREATE TABLE IF NOT EXISTS `requests` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `passengerName` TEXT NOT NULL, `rating` REAL NOT NULL,
                      `pickup` TEXT NOT NULL, `seats` INTEGER NOT NULL,
                      `status` TEXT NOT NULL, `routeId` INTEGER NOT NULL,
                      `shift` TEXT NOT NULL, `direction` TEXT NOT NULL,
                      `code` TEXT NOT NULL, `validated` INTEGER NOT NULL,
                      `passengerType` TEXT NOT NULL
                    )
                """.trimIndent())
                old.execSQL("""
                    CREATE TABLE IF NOT EXISTS `users` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `name` TEXT NOT NULL, `email` TEXT NOT NULL,
                      `passwordHash` TEXT NOT NULL, `type` TEXT NOT NULL,
                      `zone` TEXT, `shift` TEXT NOT NULL, `entry` TEXT NOT NULL,
                      `exit` TEXT NOT NULL, `privacy` INTEGER NOT NULL
                    )
                """.trimIndent())
                old.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reviews` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `refKey` TEXT NOT NULL, `role` TEXT NOT NULL,
                      `fromName` TEXT NOT NULL, `toName` TEXT NOT NULL,
                      `stars` INTEGER NOT NULL, `tags` TEXT NOT NULL,
                      `comment` TEXT NOT NULL, `routeTitle` TEXT NOT NULL,
                      `dateLabel` TEXT NOT NULL
                    )
                """.trimIndent())
                old.execSQL("""
                    INSERT INTO routes (id,origin,destination,departure,arrival,price,seats,
                      driverName,driverRating,car,stops,status,ownedByMe,alternative,tag,
                      days,recurring,waitMinutes,progress,waitStartedAt)
                    VALUES (11,'Coyote','UTTT','07:00','08:00',20,2,'Ana',5.0,'Auto','',
                      'ACTIVE',0,0,NULL,'Lunes',0,5,0,0)
                """.trimIndent())
                old.execSQL("""
                    INSERT INTO bookings (id,code,routeId,origin,destination,departure,pickup,
                      driverName,car,seats,total,status,progress,rated,seeded,dateLabel,
                      shift,direction)
                    VALUES (21,'V3-BOOK',11,'Coyote','UTTT','07:00','Coyote','Ana','Auto',
                      1,20,'CONFIRMED',0,0,0,'Hoy','MATUTINO','ENTRADA')
                """.trimIndent())
                old.execSQL("""
                    INSERT INTO requests (id,passengerName,rating,pickup,seats,status,
                      routeId,shift,direction,code,validated,passengerType)
                    VALUES (31,'Beto',4.0,'Centro',1,'PENDING',11,'MATUTINO',
                      'ENTRADA','V3-REQ',0,'Alumno')
                """.trimIndent())
                old.execSQL("""
                    INSERT INTO users (id,name,email,passwordHash,type,zone,shift,entry,
                      exit,privacy)
                    VALUES (41,'Caro','caro@uttt.edu.mx','demo-hash','PASSENGER',NULL,
                      'MATUTINO','07:00','14:00',1)
                """.trimIndent())
                old.execSQL("""
                    INSERT INTO reviews (id,refKey,role,fromName,toName,stars,tags,
                      comment,routeTitle,dateLabel)
                    VALUES (51,'v3-review','DRIVER','Beto','Ana',5,'','Bien','Coyote','Hoy')
                """.trimIndent())
                old.version = 3
            }

            val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(MIGRATION_3_4)
                .build()
            try {
                // Abrir la base activa tanto la migración como la validación de Room v4.
                val sqlite = upgraded.openHelper.writableDatabase
                assertEquals(4, sqlite.version)
                for ((table, expectedId) in listOf(
                    "routes" to 11, "bookings" to 21, "requests" to 31,
                    "users" to 41, "reviews" to 51
                )) {
                    sqlite.query("SELECT COUNT(*) FROM `$table` WHERE id = $expectedId").use {
                        assertTrue(it.moveToFirst())
                        assertEquals("El registro de $table debe conservarse", 1, it.getInt(0))
                    }
                }
                for (table in listOf("routes", "bookings", "requests", "reviews")) {
                    sqlite.query("SELECT ownerUserId FROM `$table` LIMIT 1").use {
                        assertTrue(it.moveToFirst())
                        assertTrue("El propietario histórico de $table debe ser NULL", it.isNull(0))
                    }
                }
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
