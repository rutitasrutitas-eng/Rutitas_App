package mx.edu.uttt.rutitas.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
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
    private val dir: File get() = File(context.filesDir, "private_photos").apply { mkdirs() }

    fun save(key: String, source: Any): File? {
        return try {
            val bitmap = decode(source) ?: return null
            val dest = File(dir, "$key.jpg")
            FileOutputStream(dest).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            bitmap.recycle()
            if (source is File) source.delete() // borra el temporal de la cámara
            dest
        } catch (e: Exception) {
            null
        }
    }

    fun fileFor(key: String): File? = File(dir, "$key.jpg").takeIf { it.exists() }

    fun delete(key: String) { File(dir, "$key.jpg").delete() }

    fun deleteAll() { dir.listFiles()?.forEach { it.delete() } }

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
