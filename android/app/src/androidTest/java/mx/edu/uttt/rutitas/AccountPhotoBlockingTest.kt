package mx.edu.uttt.rutitas

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import mx.edu.uttt.rutitas.data.PrivatePhotoStore
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class AccountPhotoBlockingTest {

    @Test
    fun deletedAccountBlocksProfileAndDriverPhotos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val isolatedDir = File(
            context.cacheDir,
            "rutitas_app02_blocking_test"
        ).apply { mkdirs() }

        val isolatedContext = object : ContextWrapper(context) {
            override fun getFilesDir(): File = isolatedDir
        }

        val store = PrivatePhotoStore(isolatedContext)
        val userId = 980041L
        val profileKey = "profile_$userId"
        val driverKey = "user_${userId}_identity"

        fun createSource(): File {
            val source = File.createTempFile(
                "photo-source-", ".jpg", context.cacheDir
            )
            val bitmap = Bitmap.createBitmap(
                32, 32, Bitmap.Config.ARGB_8888
            )
            try {
                FileOutputStream(source).use { output ->
                    assertTrue(
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            output
                        )
                    )
                }
            } finally {
                bitmap.recycle()
            }
            return source
        }

        try {
            store.unblockAccountPhotos(userId)

            assertNotNull(
                store.save(profileKey, createSource())
            )
            assertNotNull(
                store.save(driverKey, createSource())
            )

            store.blockAccountPhotos(userId)

            assertNull(store.fileFor(profileKey))
            assertNull(store.fileFor(driverKey))

            val rejectedProfile = createSource()
            val rejectedDriver = createSource()

            try {
                assertNull(
                    store.save(profileKey, rejectedProfile)
                )
                assertNull(
                    store.save(driverKey, rejectedDriver)
                )
            } finally {
                rejectedProfile.delete()
                rejectedDriver.delete()
            }

            assertNull(store.fileFor(profileKey))
            assertNull(store.fileFor(driverKey))

            store.unblockAccountPhotos(userId)

            assertNotNull(
                store.save(profileKey, createSource())
            )
            assertNotNull(
                store.save(driverKey, createSource())
            )
        } finally {
            store.blockAccountPhotos(userId)
            store.unblockAccountPhotos(userId)
            isolatedDir.deleteRecursively()
        }
    }
}