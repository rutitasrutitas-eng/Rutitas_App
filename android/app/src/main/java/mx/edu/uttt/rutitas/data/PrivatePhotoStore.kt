package mx.edu.uttt.rutitas.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.AtomicFile
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Guarda las fotos sensibles (credencial, documentos) SOLO en el almacenamiento privado de la app:
 *  - no aparecen en la galería ni las puede leer otra app,
 *  - se corrige la rotación y se eliminan los metadatos EXIF (ubicación GPS, modelo del teléfono),
 *  - se reducen a un tamaño razonable,
 *  - se pueden borrar individualmente o junto con la cuenta.
 */
@Singleton
class PrivatePhotoStore @Inject constructor(@ApplicationContext private val context: Context) {
    private companion object {
        val diskLock = Any()
        val validKey = Regex("[A-Za-z0-9_-]{1,100}")
        val deletedAccounts = mutableSetOf<Long>()
        val accountVersions = mutableMapOf<Long, Long>()
        val profileKeyPattern = Regex("""^profile_(-?\d+)$""")
        val driverKeyPattern = Regex("""^user_(-?\d+)_[A-Za-z0-9_-]+$""")
    }
    private val dir: File get() = File(context.filesDir, "private_photos").apply { mkdirs() }

    fun accountPhotoVersion(userId: Long): Long = synchronized(diskLock) {
        accountVersions[userId] ?: 0L
    }

    fun save(
        key: String,
        source: Any,
        expectedVersion: Long? = null
    ): File? = synchronized(diskLock) {
        if (!validKey.matches(key)) return@synchronized null
        val ownerId = profileKeyPattern.matchEntire(key)?.groupValues?.get(1)?.toLongOrNull()
            ?: driverKeyPattern.matchEntire(key)?.groupValues?.get(1)?.toLongOrNull()
        if (ownerId != null && ownerId in deletedAccounts) return@synchronized null
        if (ownerId != null && expectedVersion != null &&
            expectedVersion != (accountVersions[ownerId] ?: 0L)
        ) return@synchronized null

        val bitmap = try {
            decode(source)
        } catch (e: Exception) {
            null
        } ?: return null

        val dest = File(dir, "$key.jpg")
        val atomic = AtomicFile(dest)

        var stream: FileOutputStream? = null

        try {
            stream = atomic.startWrite()

            val compressed = bitmap.compress(
                Bitmap.CompressFormat.JPEG, 85, stream
            )

            if (!compressed) {
                throw java.io.IOException("No se pudo comprimir la imagen.")
            }

            atomic.finishWrite(stream)
            stream = null

            if (source is File &&
                source.canonicalPath != dest.canonicalPath) {
                source.delete()
            }

            dest
        } catch (e: Exception) {
            if (stream != null) {
                atomic.failWrite(stream)
            }
            null
        } finally {
            bitmap.recycle()
        }
    }

    /** Bloquea nuevas fotos y elimina los archivos del usuario bajo diskLock. */
    fun blockAccountPhotos(userId: Long) = synchronized(diskLock) {
        deletedAccounts.add(userId)
        accountVersions[userId] = (accountVersions[userId] ?: 0L) + 1L
        AtomicFile(File(dir, "profile_${userId}.jpg")).delete()
        deleteForUser(userId)
    }

    /** Recuperacion explicita de una cuenta DEMO. */
    fun unblockAccountPhotos(userId: Long) = synchronized(diskLock) {
        deletedAccounts.remove(userId)
    }

    fun fileFor(key: String): File? = synchronized(diskLock) {
        if (!validKey.matches(key)) return@synchronized null
        File(dir, "$key.jpg").takeIf { it.isFile }
    }

    fun delete(key: String) = synchronized(diskLock) {
        if (validKey.matches(key)) {
            AtomicFile(File(dir, "$key.jpg")).delete()
        }
    }

    /** Solo fotos de conductor con nombre perteneciente al usuario. La foto de perfil se gestiona aparte. */
    fun deleteForUser(userId: Long) = synchronized(diskLock) {
        val prefix = "user_${userId}_"
        val allowed = Regex(
            """^${Regex.escape(prefix)}[A-Za-z0-9_-]+\.jpg(?:\.new|\.bak)?$"""
        )

        dir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && allowed.matches(it.name) }
            ?.map { file ->
                file.name
                    .removeSuffix(".new")
                    .removeSuffix(".bak")
            }
            ?.distinct()
            ?.forEach { baseName ->
                AtomicFile(File(dir, baseName)).delete()
            }
    }

    /** Exclusivamente para reinicialización explícita de datos de prueba; NO usar en baja de cuenta. */
    fun deleteAll() = synchronized(diskLock) {
        dir.listFiles()?.forEach { it.delete() }
    }

    private fun open(source: Any): InputStream? = when (source) {
        is File -> source.inputStream()
        is Uri -> context.contentResolver.openInputStream(source)
        else -> null
    }

    private fun decode(source: Any): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > 2000) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = open(source)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        val orientation = try {
            open(source)?.use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
                ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) { ExifInterface.ORIENTATION_NORMAL }
        val m = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        }
        return if (m.isIdentity) bmp else Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
}
