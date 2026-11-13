package mx.edu.uttt.rutitas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object RColors {
    val Blue = Color(0xFF2563EB)
    val DarkBlue = Color(0xFF1E3A8A)
    val Coral = Color(0xFFFF6B6B)
    val Yellow = Color(0xFFFFD166)
    val Background = Color(0xFFF8FAFC)
    val Text = Color(0xFF172033)
    val Card = Color(0xFFFFFFFF)
    val Muted = Color(0xFF64748B)
    val Line = Color(0xFFE2E8F0)
    val BlueSoft = Color(0xFFEAF1FF)
    val CoralSoft = Color(0xFFFFEAEA)
}

private val scheme = lightColorScheme(
    primary = RColors.Blue, onPrimary = Color.White,
    primaryContainer = RColors.BlueSoft, onPrimaryContainer = RColors.DarkBlue,
    secondary = RColors.DarkBlue, onSecondary = Color.White,
    tertiary = RColors.Yellow, onTertiary = RColors.Text,
    error = RColors.Coral, onError = Color.White,
    background = RColors.Background, onBackground = RColors.Text,
    surface = RColors.Card, onSurface = RColors.Text,
    surfaceVariant = Color(0xFFEFF3F9), onSurfaceVariant = RColors.Muted,
    outline = Color(0xFFCBD5E1), outlineVariant = RColors.Line
)

private val base = Typography()
private val typography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
fun RutitasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp)
        ),
        content = content
    )
}
