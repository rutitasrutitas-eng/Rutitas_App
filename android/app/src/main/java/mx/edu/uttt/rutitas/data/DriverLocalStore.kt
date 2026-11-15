package mx.edu.uttt.rutitas.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import mx.edu.uttt.rutitas.BuildConfig
import mx.edu.uttt.rutitas.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistencia local exclusiva del modo DEMO.
 * No representa una aprobacion real del servidor.
 */
@Singleton
class DriverLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(
        "rutitas_driver_demo",
        Context.MODE_PRIVATE
    )

    fun save(userId: Long, session: Session) {
        if (!BuildConfig.DEMO_MODE) return
        if (session.user?.id != userId) return

        val key = "driver_${userId}_"

        val editor = prefs.edit()
            .putString(key + "status", session.driverStatus.name)
            .putString(key + "brand", session.vehicle.brand)
            .putString(key + "model", session.vehicle.model)
            .putString(key + "year", session.vehicle.year)
            .putString(key + "color", session.vehicle.color)
            .putString(key + "plates", session.vehicle.plates)
            .putInt(key + "seats", session.vehicle.seats)

        session.documents.forEach { (kind, status) ->
            editor.putString(key + "doc_" + kind.name, status.name)
        }

        editor.apply()
    }

    fun restore(session: Session): Session {
        if (!BuildConfig.DEMO_MODE) return session
        val userId = session.user?.id ?: return session
        val key = "driver_${userId}_"

        if (!prefs.contains(key + "status")) return session

        val status = enumValueOrDefault(
            prefs.getString(key + "status", null),
            DriverStatus.NONE
        )

        val vehicle = Vehicle(
            brand = prefs.getString(key + "brand", "") ?: "",
            model = prefs.getString(key + "model", "") ?: "",
            year = prefs.getString(key + "year", "") ?: "",
            color = prefs.getString(key + "color", "") ?: "",
            plates = prefs.getString(key + "plates", "") ?: "",
            seats = prefs.getInt(key + "seats", 3)
        )

        val docs = DocKind.entries.associateWith { kind ->
            enumValueOrDefault(
                prefs.getString(key + "doc_" + kind.name, null),
                DocStatus.PENDING
            )
        }

        return session.copy(
            driverStatus = status,
            vehicle = vehicle,
            documents = docs
        )
    }

    fun clear(userId: Long) {
        val prefix = "driver_${userId}_"
        val editor = prefs.edit()

        prefs.all.keys
            .filter { it.startsWith(prefix) }
            .forEach { editor.remove(it) }

        editor.apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        value: String?,
        default: T
    ): T = enumValues<T>().firstOrNull { it.name == value } ?: default
}
