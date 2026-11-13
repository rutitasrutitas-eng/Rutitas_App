
package mx.edu.uttt.rutitas.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración de Room versión 3 a versión 4.
 *
 * Agrega ownerUserId a las tablas necesarias.
 * Conserva los registros existentes.
 * Los registros históricos quedan sin propietario
 * verificable cuando la columna es nueva.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {

        addColumnIfMissing(db, "routes", "ownerUserId")
        addColumnIfMissing(db, "bookings", "ownerUserId")
        addColumnIfMissing(db, "requests", "ownerUserId")
        addColumnIfMissing(db, "reviews", "ownerUserId")
    }

    private fun addColumnIfMissing(
        db: SupportSQLiteDatabase,
        table: String,
        column: String
    ) {
        var exists = false

        db.query("PRAGMA table_info($table)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")

            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) {
                    exists = true
                    break
                }
            }
        }

        if (!exists) {
            db.execSQL(
                "ALTER TABLE $table ADD COLUMN $column INTEGER DEFAULT NULL"
            )
        }
    }
}
