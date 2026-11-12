package mx.edu.uttt.rutitas.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import mx.edu.uttt.rutitas.R

/** Duración del GIF animado (192 cuadros ≈ 6.4 s). Se muestra una sola vez al abrir la app. */
const val SPLASH_DURATION_MS = 6400L

/** Pantalla de bienvenida: reproduce la animación de Rutitas. Tocar la pantalla la omite. */
@Composable
fun SplashScreen(onSkip: () -> Unit) {
    val context = LocalContext.current
    val loader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }
    Box(
        Modifier.fillMaxSize().background(Color(0xFFFFFEFE))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSkip)
    ) {
        AsyncImage(
            model = R.raw.animacion_rutitas,
            imageLoader = loader,
            contentDescription = "Rutitas",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
