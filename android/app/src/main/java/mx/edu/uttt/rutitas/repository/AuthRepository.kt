package mx.edu.uttt.rutitas.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.SessionManager
import mx.edu.uttt.rutitas.database.BookingDao
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import mx.edu.uttt.rutitas.database.RequestDao
import mx.edu.uttt.rutitas.database.ReviewDao
import mx.edu.uttt.rutitas.database.RouteDao
import mx.edu.uttt.rutitas.database.UserDao
import mx.edu.uttt.rutitas.database.UserEntity
import mx.edu.uttt.rutitas.database.toProfile
import mx.edu.uttt.rutitas.model.*
import mx.edu.uttt.rutitas.utils.AppConfig
import mx.edu.uttt.rutitas.utils.sha256
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val bookingDao: BookingDao,
    private val routeDao: RouteDao,
    private val requestDao: RequestDao,
    private val reviewDao: ReviewDao,
    private val photoStore: PrivatePhotoStore,
    private val driverRepo: DriverRepository,
    private val seeder: DemoSeeder,
    private val session: SessionManager,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("rutitas_prefs", Context.MODE_PRIVATE)

    private fun photoKey(userId: Long) = "profile_$userId"

    /** Carga la foto de perfil guardada (si existe) para el usuario de la sesión. */
    private fun loadPhoto() {
        val id = session.current.user?.id ?: return
        session.update { it.copy(profilePhoto = photoStore.fileFor(photoKey(id))) }
    }

    suspend fun setProfilePhoto(source: Any) {
        val id = session.current.user?.id ?: return
        val f = withContext(Dispatchers.IO) { photoStore.save(photoKey(id), source) }
        if (f != null) session.update { it.copy(profilePhoto = f) }
    }

    fun removeProfilePhoto() {
        val id = session.current.user?.id ?: return
        photoStore.delete(photoKey(id))
        session.update { it.copy(profilePhoto = null) }
    }

    /** Edita nombre, correo, tipo de usuario, zona y horario. */
    suspend fun updateProfile(
        name: String, email: String, type: UserType, zone: String?, shift: Shift, entry: String, exit: String
    ): Result<Unit> {
        val u = session.current.user ?: return Result.failure(IllegalStateException("Inicia sesión primero."))
        val newName = name.trim()
        val mail = email.trim().lowercase()
        if (newName.isBlank()) return Result.failure(IllegalArgumentException("El nombre no puede estar vacío."))
        if (u.id > 0) {
            if (!AppConfig.isInstitutionalEmail(mail)) {
                return Result.failure(IllegalArgumentException("Usa el correo de tu institución (ej. nombre@uttt.edu.mx)."))
            }
            if (mail != u.email &&
                (mail == DemoData.DEMO_EMAIL || mail == DemoData.BLOCKED_EMAIL || userDao.findByEmail(mail) != null)
            ) return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo."))
        } else if (mail != u.email) {
            return Result.failure(IllegalStateException("El correo de las cuentas demo no se puede cambiar."))
        }
        if (newName != u.name) {
            reviewDao.renameFrom(u.name, newName)
            reviewDao.renameTo(u.name, newName)
        }
        session.update {
            it.copy(
                user = u.copy(name = newName, email = mail, type = type, university = AppConfig.institutionFor(mail) ?: u.university),
                zone = zone, shift = shift, entryTime = entry, exitTime = exit
            )
        }
        if (u.id > 0) {
            userDao.get(u.id)?.let {
                userDao.update(it.copy(name = newName, email = mail, type = type.name, zone = zone, shift = shift.name, entry = entry, exit = exit))
            }
        }
        return Result.success(Unit)
    }

    private fun isDeleted(email: String) = prefs.getBoolean("deleted_$email", false)

    /** Devuelve Result<true> si la cuenta inició sesión pero está inhabilitada. */
    suspend fun login(email: String, password: String): Result<Boolean> {
        delay(500)
        val mail = email.trim().lowercase()
        if ((mail == DemoData.DEMO_EMAIL || mail == DemoData.BLOCKED_EMAIL) && isDeleted(mail)) {
            return Result.failure(IllegalStateException("Esta cuenta fue eliminada. Pulsa «Restaurar cuentas demo» para volver a usarla."))
        }
        if (mail == DemoData.DEMO_EMAIL && password == DemoData.DEMO_PASSWORD) {
            session.update {
                Session(
                    user = DemoData.demoUser, zone = "Coyote", shift = Shift.MATUTINO,
                    entryTime = "7:00 AM", exitTime = "2:00 PM", privacyAccepted = true
                )
            }
            loadPhoto()
            return Result.success(false)
        }
        if (mail == DemoData.BLOCKED_EMAIL && password == DemoData.DEMO_PASSWORD) {
            session.update {
                Session(
                    user = DemoData.blockedUser, zone = "Tlahuelilpan", privacyAccepted = true,
                    accountStatus = AccountStatus.DISABLED, disabledReason = "Documento vencido"
                )
            }
            loadPhoto()
            return Result.success(true)
        }
        val u = userDao.findByEmail(mail)
        if (u == null || u.passwordHash != sha256(password)) {
            return Result.failure(IllegalArgumentException("Correo o contraseña incorrectos."))
        }
        session.update {
            Session(
                user = u.toProfile(), zone = u.zone, shift = Shift.valueOf(u.shift),
                entryTime = u.entry, exitTime = u.exit, privacyAccepted = u.privacy
            )
        }
        loadPhoto()
            return Result.success(false)
    }

    suspend fun register(name: String, email: String, password: String, type: UserType): Result<Unit> {
        delay(400)
        val mail = email.trim().lowercase()
        if (!AppConfig.isInstitutionalEmail(mail)) {
            return Result.failure(IllegalArgumentException("Usa el correo de tu institución (ej. nombre@uttt.edu.mx)."))
        }
        if (mail == DemoData.DEMO_EMAIL || mail == DemoData.BLOCKED_EMAIL || userDao.findByEmail(mail) != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo."))
        }
        val id = userDao.insert(
            UserEntity(
                name = name.trim(), email = mail, passwordHash = sha256(password), type = type.name,
                zone = null, shift = Shift.MATUTINO.name, entry = Shift.MATUTINO.entry,
                exit = Shift.MATUTINO.exit, privacy = false
            )
        )
        session.update { Session(user = UserProfile(id, name.trim(), mail, type, AppConfig.institutionFor(mail) ?: "Institución participante")) }
        return Result.success(Unit)
    }

    fun logout() = session.update { Session() }

    /**
     * Elimina la cuenta de forma COMPLETA: perfil, reservas, rutas publicadas, solicitudes,
     * registro de conductor y fotos privadas. Las cuentas demo también quedan eliminadas
     * (se pueden recuperar con «Restaurar cuentas demo» en la pantalla de inicio de sesión).
     */
    suspend fun deleteAccount() {
        val u = session.current.user
        if (u != null) {
            reviewDao.deleteInvolving(u.name)
            photoStore.delete(photoKey(u.id))
            if (u.id > 0) {
                userDao.delete(u.id)
                // Libera los lugares de las reservas activas creadas por el usuario
                bookingDao.activeCreated().forEach { b ->
                    routeDao.get(b.routeId)?.let { routeDao.update(it.copy(seats = it.seats + b.seats)) }
                }
                bookingDao.deleteCreated()
            } else {
                prefs.edit().putBoolean("deleted_${u.email}", true).apply()
                bookingDao.clear()
            }
        }
        routeDao.deleteOwned()
        requestDao.clear()
        requestDao.insertAll(DemoData.requests())
        driverRepo.resetDemoState() // borra también las fotos privadas
        session.update { Session() }
    }

    /** Recupera las cuentas demo eliminadas y restablece los datos de ejemplo. */
    fun restoreDemoAccounts() {
        prefs.edit().remove("deleted_${DemoData.DEMO_EMAIL}").remove("deleted_${DemoData.BLOCKED_EMAIL}").apply()
        seeder.reset()
    }

    suspend fun savePrefs(
        zone: String? = null, shift: Shift? = null, entry: String? = null, exit: String? = null,
        privacy: Boolean? = null
    ) {
        session.update {
            it.copy(
                zone = zone ?: it.zone, shift = shift ?: it.shift,
                entryTime = entry ?: it.entryTime, exitTime = exit ?: it.exitTime,
                privacyAccepted = privacy ?: it.privacyAccepted
            )
        }
        val id = session.current.user?.id ?: return
        if (id <= 0) return // las cuentas demo no se guardan en Room
        val s = session.current
        userDao.get(id)?.let {
            userDao.update(
                it.copy(zone = s.zone, shift = s.shift.name, entry = s.entryTime, exit = s.exitTime, privacy = s.privacyAccepted)
            )
        }
    }

    suspend fun updateName(name: String) {
        val u = session.current.user ?: return
        session.update { it.copy(user = u.copy(name = name.trim())) }
        if (u.id > 0) userDao.get(u.id)?.let { userDao.update(it.copy(name = name.trim())) }
    }
}
