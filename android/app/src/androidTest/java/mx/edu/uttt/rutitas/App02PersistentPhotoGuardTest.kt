package mx.edu.uttt.rutitas

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class App02PersistentPhotoGuardTest {

    @Test
    fun guardSurvivesStoreRecreationAndPhotoCleanup() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val isolatedDir = File(
            app.cacheDir,
            "app02_persistent_guard_825"
        )

        isolatedDir.deleteRecursively()
        assertTrue(isolatedDir.mkdirs())

        val context = object : ContextWrapper(app) {
            override fun getFilesDir(): File = isolatedDir
        }

        fun source(name: String): File {
            val file = File(isolatedDir, "$name.jpg")
            val bitmap = Bitmap.createBitmap(
                32, 32, Bitmap.Config.ARGB_8888
            )
            try {
                FileOutputStream(file).use { output ->
                    assertTrue(bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 90, output
                    ))
                }
            } finally {
                bitmap.recycle()
            }
            return file
        }

        try {
            val userId = 9908241L
            val key = "profile_$userId"

            val first = PrivatePhotoStore(context)
            val oldVersion = first.accountPhotoVersion(userId)

            assertNotNull(
                first.save(key, source("before"), oldVersion)
            )

            first.blockAccountPhotos(userId)
            assertNull(first.fileFor(key))

            // La limpieza de fotos no debe borrar el bloqueo.
            first.deleteAll()

            // Simula recrear el servicio sobre el mismo disco.
            val second = PrivatePhotoStore(context)

            assertTrue(
                second.accountPhotoVersion(userId) > oldVersion
            )

            assertNull(second.fileFor(key))
            assertNull(
                second.save(
                    key,
                    source("blocked"),
                    second.accountPhotoVersion(userId)
                )
            )

            // La restauracion no debe aceptar operaciones antiguas.
            second.unblockAccountPhotos(userId)

            val third = PrivatePhotoStore(context)

            assertNull(
                third.save(key, source("stale"), oldVersion)
            )

            assertNotNull(
                third.save(
                    key,
                    source("restored"),
                    third.accountPhotoVersion(userId)
                )
            )

            assertNotNull(third.fileFor(key))
        } finally {
            isolatedDir.deleteRecursively()
        }
    }
}