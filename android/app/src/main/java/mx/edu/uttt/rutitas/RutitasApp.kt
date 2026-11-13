package mx.edu.uttt.rutitas

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import mx.edu.uttt.rutitas.data.DemoSeeder
import javax.inject.Inject

@HiltAndroidApp
class RutitasApp : Application() {
    @Inject lateinit var seeder: DemoSeeder

    override fun onCreate() {
        super.onCreate()
        seeder.seedIfNeeded()
    }
}
