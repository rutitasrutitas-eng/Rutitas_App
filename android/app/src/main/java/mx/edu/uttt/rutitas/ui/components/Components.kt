package mx.edu.uttt.rutitas.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.delay
import mx.edu.uttt.rutitas.model.RideRequest
import mx.edu.uttt.rutitas.utils.TimeUtils
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.utils.seatsText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutitasTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = RColors.Background)
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, loading: Boolean = false, color: Color = RColors.Blue
) {
    Button(
        onClick = onClick, enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RColors.Blue)
    ) { Text(text, color = RColors.Blue, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun TagChip(text: String, bg: Color = RColors.Yellow, fg: Color = RColors.Text) {
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), color = fg,
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusChip(label: String) {
    val (bg, fg) = when (label) {
        "Confirmado", "Completado", "Aceptada", "Activa", "Validado" -> Color(0xFFDBEAFE) to RColors.DarkBlue
        "Pendiente", "En camino", "En curso" -> Color(0xFFFFF1C9) to Color(0xFF7A5900)
        "Finalizado", "Finalizada" -> Color(0xFFE2E8F0) to Color(0xFF475569)
        "Cancelado", "Cancelada", "Revisar", "Interrumpida", "Rechazada", "Rechazado" -> RColors.CoralSoft to Color(0xFFB42323)
        else -> Color(0xFFE2E8F0) to Color(0xFF475569)
    }
    TagChip(label, bg, fg)
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier, color: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(1.dp)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
fun RutitasField(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text, password: Boolean = false,
    error: String? = null, enabled: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    val supporting: (@Composable () -> Unit)? = if (error != null) { { Text(error) } } else null
    val trailing: (@Composable () -> Unit)? = if (password) {
        {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Mostrar")
            }
        }
    } else null
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth(), singleLine = true, enabled = enabled,
        isError = error != null, supportingText = supporting, trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun RouteCard(route: Route, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (route.tag != null) { TagChip(route.tag); Spacer(Modifier.height(8.dp)) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(route.title, style = MaterialTheme.typography.titleMedium)
                    Text("${route.departure} → ${route.arrival}", color = RColors.Muted, style = MaterialTheme.typography.bodyMedium)
                    Text("${seatsText(route.seats)} · ${route.driverName}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("\$${route.price}", style = MaterialTheme.typography.titleLarge, color = RColors.Blue)
                    if (route.status == RouteStatus.INTERRUPTED) StatusChip("Interrumpida")
                    else Text("por persona", color = RColors.Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun DisabledBanner(onDetails: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RColors.CoralSoft)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = RColors.Coral)
            Spacer(Modifier.width(10.dp))
            Text("Cuenta inhabilitada. Esta acción no está disponible.", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onDetails) { Text("Ver más") }
        }
    }
}

data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val passengerItems = listOf(
    NavItem(Routes.MAP, "Mapa", Icons.Filled.Map),
    NavItem(Routes.SEARCH, "Buscar", Icons.Filled.Search),
    NavItem(Routes.TRIPS, "Viajes", Icons.Filled.DirectionsCar),
    NavItem(Routes.PROFILE, "Perfil", Icons.Filled.Person)
)
private val driverItems = listOf(
    NavItem(Routes.DRIVER_HOME, "Inicio", Icons.Filled.Home),
    NavItem(Routes.DRIVER_ROUTES, "Rutas", Icons.Filled.AltRoute),
    NavItem(Routes.DRIVER_REQUESTS, "Solicitudes", Icons.Filled.People),
    NavItem(Routes.DRIVER_PROFILE, "Perfil", Icons.Filled.Person)
)

@Composable
private fun RBottomBar(items: List<NavItem>, selected: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.route == selected, onClick = { onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, indicatorColor = RColors.Blue,
                    selectedTextColor = RColors.Blue
                )
            )
        }
    }
}

@Composable fun PassengerBar(selected: String, onSelect: (String) -> Unit) = RBottomBar(passengerItems, selected, onSelect)
@Composable fun DriverBar(selected: String, onSelect: (String) -> Unit) = RBottomBar(driverItems, selected, onSelect)

@Composable
fun StarRow(rating: Int, onChange: ((Int) -> Unit)? = null, size: Int = 40) {
    Row {
        for (i in 1..5) {
            val icon = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder
            val tint = if (i <= rating) RColors.Yellow else RColors.Line
            if (onChange != null) {
                IconButton(onClick = { onChange(i) }, modifier = Modifier.size((size + 8).dp)) {
                    Icon(icon, "$i estrellas", tint = tint, modifier = Modifier.size(size.dp))
                }
            } else Icon(icon, null, tint = tint, modifier = Modifier.size(size.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { o ->
            FilterChip(
                selected = o in selected, onClick = { onToggle(o) }, label = { Text(o) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RColors.Blue, selectedLabelColor = Color.White
                )
            )
        }
    }
}

/** state: 0 completado, 1 actual, 2 pendiente */
@Composable
fun TimelineRow(title: String, subtitle: String?, state: Int, last: Boolean) {
    val color = when (state) { 0 -> RColors.Blue; 1 -> RColors.Yellow; else -> RColors.Line }
    Row(Modifier.fillMaxWidth().height(52.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(18.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                if (state == 0) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            if (!last) Box(Modifier.width(2.dp).weight(1f).background(RColors.Line))
        }
        Spacer(Modifier.width(12.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = if (state == 1) FontWeight.Bold else FontWeight.Normal)
            if (subtitle != null) StatusChip(subtitle)
        }
    }
}

@Composable
fun Avatar(name: String, size: Int = 44, photo: File? = null) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(RColors.BlueSoft),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            AsyncImage(model = photo, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(name.take(1).uppercase(), color = RColors.DarkBlue, fontWeight = FontWeight.Bold, fontSize = (size / 2.4).sp)
        }
    }
}

@Composable
fun PhotoBox(photo: Any?, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.fillMaxWidth().height(170.dp).clip(shape).background(RColors.BlueSoft).border(1.dp, RColors.Line, shape),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            AsyncImage(model = photo, contentDescription = label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = RColors.Blue, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(6.dp))
                Text(label, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

class PhotoActions(val take: () -> Unit, val pick: () -> Unit)

/**
 * Tomar foto a resolución completa (pide permiso de cámara; el archivo temporal queda en una carpeta
 * privada de la app) o elegir de la galería. onPhoto recibe un File (cámara) o un Uri (galería).
 */
@Composable
fun rememberPhotoActions(onPhoto: (Any) -> Unit): PhotoActions {
    val context = LocalContext.current
    val current by rememberUpdatedState(onPhoto)
    var pending by remember { mutableStateOf<File?>(null) }
    val takeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        pending = null
        if (ok && f != null) current(f) else f?.delete()
    }
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) current(uri)
    }
    fun launchCamera() {
        val dir = File(context.cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "tmp_${System.nanoTime()}.jpg")
        pending = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takeLauncher.launch(uri)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) launchCamera()
        else Toast.makeText(context, "Permiso de cámara rechazado. Usa «Elegir imagen».", Toast.LENGTH_LONG).show()
    }
    return remember {
        PhotoActions(
            take = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (granted) launchCamera() else permLauncher.launch(Manifest.permission.CAMERA)
            },
            pick = { pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
    }
}

fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Mientras esta pantalla está visible se bloquean capturas y grabaciones de pantalla (FLAG_SECURE). */
@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

/** Campo de hora con reloj (no hay que escribir). Devuelve el texto como "7:00 AM". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    value: String, onValueChange: (String) -> Unit, label: String,
    modifier: Modifier = Modifier, error: String? = null
) {
    var open by remember { mutableStateOf(false) }
    val supporting: (@Composable () -> Unit)? = if (error != null) { { Text(error) } } else null
    Box(modifier) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, isError = error != null,
            supportingText = supporting,
            trailingIcon = { Icon(Icons.Filled.Schedule, contentDescription = "Elegir hora", tint = RColors.Blue) },
            shape = RoundedCornerShape(14.dp)
        )
        Box(Modifier.matchParentSize().clickable { open = true })
    }
    if (open) {
        val minutes = TimeUtils.toMinutes(value) ?: (7 * 60)
        val state = rememberTimePickerState(initialHour = minutes / 60, initialMinute = minutes % 60, is24Hour = false)
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(TimeUtils.format(state.hour * 60 + state.minute))
                    open = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar") } }
        )
    }
}

/** Código de reserva grande para mostrarlo al conductor. */
@Composable
fun CodeDialog(code: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tu código de abordaje") },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = RColors.Yellow, shape = RoundedCornerShape(16.dp)) {
                    Text(code, Modifier.padding(horizontal = 24.dp, vertical = 16.dp), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = RColors.Text)
                }
                Text("Muéstraselo al conductor para abordar.", color = RColors.Muted)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

/** Datos del pasajero para que el conductor sepa quién es. */
@Composable
fun PassengerInfoDialog(request: RideRequest, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(request.passengerName, 48)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(request.passengerName, style = MaterialTheme.typography.titleLarge)
                    Text("⭐ ${request.rating}", color = RColors.Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoLine("Tipo de usuario", request.passengerType)
                InfoLine("Punto de encuentro", request.pickup)
                InfoLine("Horario", request.shift.label)
                InfoLine("Viaje de", "${request.direction.label} (${request.direction.hint})")
                InfoLine("Lugares", if (request.seats == 1) "1 lugar" else "${request.seats} lugares")
                InfoLine("Estado", if (request.validated) "Validado" else request.status.label)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), color = RColors.Muted, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Warning, null, tint = RColors.Coral) },
        title = { Text("¿Eliminar tu cuenta?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Se borrarán tu perfil, reservas, rutas publicadas y datos de conductor. Esta acción no se puede deshacer.")
                OutlinedTextField(
                    value = text, onValueChange = { text = it }, singleLine = true,
                    label = { Text("Escribe ELIMINAR para confirmar") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = text.trim() == "ELIMINAR") {
                Text("Eliminar cuenta", color = RColors.Coral)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


/** Segundos restantes del tiempo de espera (null si el conductor aún no llegó al punto). */
@Composable
fun waitRemainingSeconds(startedAt: Long, waitMinutes: Int): Int? {
    var now by remember(startedAt) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        if (startedAt > 0L) while (true) { now = System.currentTimeMillis(); delay(1000) }
    }
    if (startedAt <= 0L) return null
    return (waitMinutes * 60 - ((now - startedAt) / 1000).toInt()).coerceAtLeast(0)
}

fun formatMmSs(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** Opciones para la foto de perfil: tomar, elegir o quitar. */
@Composable
fun PhotoSourceDialog(onDismiss: () -> Unit, onTake: () -> Unit, onPick: () -> Unit, onRemove: (() -> Unit)?) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto de perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onDismiss(); onTake() }) { Icon(Icons.Filled.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Tomar foto") }
                TextButton(onClick = { onDismiss(); onPick() }) { Icon(Icons.Filled.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Elegir de la galería") }
                if (onRemove != null) {
                    TextButton(onClick = { onDismiss(); onRemove() }) {
                        Icon(Icons.Filled.Delete, null, tint = RColors.Coral); Spacer(Modifier.width(8.dp)); Text("Quitar foto", color = RColors.Coral)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Diálogo para calificar (estrellas, etiquetas y comentario opcional). */
@Composable
fun RateDialog(
    title: String, subtitle: String, tagOptions: List<String>,
    onDismiss: () -> Unit, onSubmit: (Int, Set<String>, String) -> Unit
) {
    var stars by remember { mutableIntStateOf(5) }
    var tags by remember { mutableStateOf(setOf<String>()) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(subtitle, color = RColors.Muted)
                StarRow(stars, { stars = it }, size = 34)
                ChipGroup(tagOptions, tags) { tags = if (it in tags) tags - it else tags + it }
                OutlinedTextField(
                    comment, { comment = it }, label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(stars, tags, comment) }) { Text("Enviar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
