package mx.edu.uttt.rutitas.repository

import android.content.Context
import androidx.room.withTransaction
import mx.edu.uttt.rutitas.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mx.edu.uttt.rutitas.data.DemoSeeder
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.data.DriverLocalStore
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
    private val driverLocalStore: DriverLocalStore,
    private val driverRepo: DriverRepository,
    private val seeder: DemoSeeder,
    private val session: SessionManager,
    private val db: AppDatabase,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("rutitas_prefs", Context.MODE_PRIVATE)

    private fun photoKey(userId: Long) = "profile_$userId"

    /** Carga la foto de perfil guardada (si existe) para el usuario de la sesiÃ³n. */
    private fun loadPhoto() {
        val userId = session.current.user?.id ?: return
        val photo = photoStore.fileFor(photoKey(userId))
        session.update { current ->
            if (current.user?.id == userId) current.copy(profilePhoto = photo) else current
        }
    }

    suspend fun setProfilePhoto(source: Any) {
        setProfilePhotoWithSaver(source) { key, value ->
            withContext(Dispatchers.IO) { photoStore.save(key, value) }
        }
    }

    internal suspend fun setProfilePhotoWithSaver(
        source: Any,
        saver: suspend (String, Any) -> java.io.File?
    ) {
        val userId = session.current.user?.id ?: return
        val photo = saver(photoKey(userId), source)
        if (photo != null) session.update { current ->
            if (current.user?.id == userId) current.copy(profilePhoto = photo) else current
        }
    }

    fun removeProfilePhoto() {
        val userId = session.current.user?.id ?: return
        photoStore.delete(photoKey(userId))
        session.update { current ->
            if (current.user?.id == userId) current.copy(profilePhoto = null) else current
        }
    }

    /** Edita nombre, correo, tipo de usuario, zona y horario. */
    suspend fun updateProfile(
        name: String, email: String, type: UserType, zone: String?, shift: Shift, entry: String, exit: String
    ): Result<Unit> {
        val original = session.current.user
            ?: return Result.failure(IllegalStateException("Inicia sesion primero."))
        val originalId = original.id
        val originalEmail = original.email
        val newName = name.trim()
        val mail = email.trim().lowercase()
        if (newName.isBlank()) return Result.failure(IllegalArgumentException("El nombre no puede estar vacio."))
        if (originalId > 0) {
            if (!AppConfig.isInstitutionalEmail(mail))
                return Result.failure(IllegalArgumentException("Usa correo institucional."))
            if (mail != originalEmail &&
                (mail == DemoData.DEMO_EMAIL || mail == DemoData.BLOCKED_EMAIL || userDao.findByEmail(mail) != null))
                return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo."))
        } else if (mail != originalEmail) {
            return Result.failure(IllegalStateException("El correo DEMO no se puede cambiar."))
        }
        if (session.current.user?.id != originalId || session.current.user?.email != originalEmail)
            return Result.failure(IllegalStateException("La sesion cambio."))
        try {
            db.withTransaction {
            if (session.current.user?.id != originalId || session.current.user?.email != originalEmail)
                error("La sesion cambio durante el guardado.")
            if (originalId > 0) {
                userDao.get(originalId)?.let { current ->
                    userDao.update(current.copy(name = newName, email = mail, type = type.name,
                        zone = zone, shift = shift.name, entry = entry, exit = exit))
                } ?: error("La cuenta ya no existe.")
            }
            if (newName != original.name) reviewDao.renameFromForOwner(originalId, original.name, newName)
        }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
        var applied = false
        session.update { current ->
            if (current.user?.id == originalId && current.user?.email == originalEmail) {
                applied = true
                current.copy(user = original.copy(name = newName, email = mail, type = type,
                    university = AppConfig.institutionFor(mail) ?: original.university),
                    zone = zone, shift = shift, entryTime = entry, exitTime = exit)
            } else current
        }
        return if (applied) Result.success(Unit)
            else Result.failure(IllegalStateException("La sesion cambio durante el guardado."))
    }

    private fun isDeleted(email: String) = prefs.getBoolean("deleted_$email", false)

    /** Devuelve Result<true> si la cuenta iniciÃ³ sesiÃ³n pero estÃ¡ inhabilitada. */
    suspend fun login(email: String, password: String): Result<Boolean> {
        delay(500)
        val mail = email.trim().lowercase()
        if ((mail == DemoData.DEMO_EMAIL || mail == DemoData.BLOCKED_EMAIL) && isDeleted(mail)) {
            return Result.failure(IllegalStateException("Esta cuenta fue eliminada. Pulsa Â«Restaurar cuentas demoÂ» para volver a usarla."))
        }
        if (mail == DemoData.DEMO_EMAIL && password == DemoData.DEMO_PASSWORD) {
            session.update {
                Session(
                    user = DemoData.demoUser, zone = "Coyote", shift = Shift.MATUTINO,
                    entryTime = "7:00 AM", exitTime = "2:00 PM", privacyAccepted = true
                )
            }
            session.update { driverLocalStore.restore(it) }
            driverRepo.restorePhotos()
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
            session.update { driverLocalStore.restore(it) }
            driverRepo.restorePhotos()
            loadPhoto()
            return Result.success(true)
        }
        val u = userDao.findByEmail(mail)
        if (u == null || u.passwordHash != sha256(password)) {
            return Result.failure(IllegalArgumentException("Correo o contraseÃ±a incorrectos."))
        }
        session.update {
            Session(
                user = u.toProfile(), zone = u.zone, shift = Shift.valueOf(u.shift),
                entryTime = u.entry, exitTime = u.exit, privacyAccepted = u.privacy
            )
        }
        session.update { driverLocalStore.restore(it) }
        driverRepo.restorePhotos()
        loadPhoto()
            return Result.success(false)
    }

    suspend fun register(name: String, email: String, password: String, type: UserType): Result<Unit> {
        delay(400)
        val mail = email.trim().lowercase()
        if (!AppConfig.isInstitutionalEmail(mail)) {
            return Result.failure(IllegalArgumentException("Usa el correo de tu instituciÃ³n (ej. nombre@uttt.edu.mx)."))
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
        session.update { Session(user = UserProfile(id, name.trim(), mail, type, AppConfig.institutionFor(mail) ?: "InstituciÃ³n participante")) }
        return Result.success(Unit)
    }

    fun logout() = session.update { Session() }

    /**
     * Baja LOCAL de la cuenta actual (DEMO). No afecta registros sin propietario
     * identificado ni archivos privados de otros usuarios; NO elimina datos del servidor.
     */
    suspend fun deleteAccount() {
        val u = session.current.user ?: return
        db.withTransaction {
            // Solo registros con ownerUserId explÃ­cito perteneciente al usuario.
            bookingDao.activeForOwner(u.id).forEach { b ->
                routeDao.restoreSeats(b.routeId, b.seats)
            }
            bookingDao.deleteForOwner(u.id)
            requestDao.deleteForOwner(u.id)
            reviewDao.deleteForOwner(u.id)
            // Las rutas con reservas ajenas se conservan CANCELLED para preservar referencias.
            // Las restantes tambiÃ©n quedan CANCELLED (borrado fÃ­sico sujeto a polÃ­tica de retenciÃ³n).
            routeDao.byOwner(u.id).forEach { route ->
                routeDao.update(route.copy(status = "CANCELLED", ownedByMe = false))
            }
            if (u.id > 0) userDao.delete(u.id)
        }
        photoStore.delete(photoKey(u.id))
        driverRepo.removeUserPhotos(u.id)
        driverLocalStore.clear(u.id)
        if (u.id <= 0) prefs.edit().putBoolean("deleted_${u.email}", true).apply()
        session.update { Session() }
    }

    /** Recupera las cuentas demo eliminadas y restablece los datos de ejemplo. */
    suspend fun restoreDemoAccounts() {
        if (!AppConfig.DEMO_MODE) throw IllegalStateException("Solo disponible en DEMO.")
        // No anuncia exito antes de terminar de reponer datos faltantes.
        seeder.reset()
        val saved = prefs.edit()
            .remove("deleted_${DemoData.DEMO_EMAIL}")
            .remove("deleted_${DemoData.BLOCKED_EMAIL}").commit()
        if (!saved) throw IllegalStateException("No se pudo restaurar la configuracion DEMO.")
        session.update { current ->
            if (current.user?.email in listOf(DemoData.DEMO_EMAIL, DemoData.BLOCKED_EMAIL))
                Session() else current
        }
    }

    suspend fun savePrefs(
        zone: String? = null, shift: Shift? = null, entry: String? = null, exit: String? = null,
        privacy: Boolean? = null
    ) {
        val before = session.current
        val originalId = before.user?.id
        val originalEmail = before.user?.email
        val next = before.copy(zone = zone ?: before.zone, shift = shift ?: before.shift,
            entryTime = entry ?: before.entryTime, exitTime = exit ?: before.exitTime,
            privacyAccepted = privacy ?: before.privacyAccepted)
        if (session.current.user?.id != originalId || session.current.user?.email != originalEmail)
            throw IllegalStateException("La sesion cambio.")
        if (originalId != null && originalId > 0) {
            userDao.get(originalId)?.let { saved ->
                userDao.update(saved.copy(zone = next.zone, shift = next.shift.name,
                    entry = next.entryTime, exit = next.exitTime, privacy = next.privacyAccepted))
            }
        }
        session.update { current ->
            if (current.user?.id == originalId && current.user?.email == originalEmail)
                current.copy(zone = next.zone, shift = next.shift, entryTime = next.entryTime,
                    exitTime = next.exitTime, privacyAccepted = next.privacyAccepted)
            else current
        }
    }

    suspend fun updateName(name: String) {
        val original = session.current.user ?: return
        val normalized = name.trim()
        if (normalized.isBlank()) return
        if (original.id > 0) userDao.get(original.id)?.let {
            userDao.update(it.copy(name = normalized))
        }
        session.update { current ->
            if (current.user?.id == original.id && current.user?.email == original.email)
                current.copy(user = original.copy(name = normalized)) else current
        }
    }
}
