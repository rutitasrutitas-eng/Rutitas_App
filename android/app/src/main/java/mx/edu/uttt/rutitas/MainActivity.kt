package mx.edu.uttt.rutitas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import mx.edu.uttt.rutitas.navigation.AppNavHost
import mx.edu.uttt.rutitas.ui.screens.SPLASH_DURATION_MS
import mx.edu.uttt.rutitas.ui.screens.SplashScreen
import mx.edu.uttt.rutitas.ui.theme.RutitasTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // splash del sistema en blanco; luego Compose reproduce el GIF
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RutitasTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) { delay(SPLASH_DURATION_MS); showSplash = false }
                Crossfade(targetState = showSplash, animationSpec = tween(400), label = "splash") { splash ->
                    if (splash) SplashScreen(onSkip = { showSplash = false }) else AppNavHost()
                }
            }
        }
    }
}
