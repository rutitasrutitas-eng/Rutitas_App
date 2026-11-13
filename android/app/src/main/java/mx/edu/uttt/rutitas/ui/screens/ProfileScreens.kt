package mx.edu.uttt.rutitas.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.ProfileViewModel

/** Avatar grande con botón de cámara para cambiar la foto de perfil. */
@Composable
fun ProfilePhotoHeader(name: String, photo: java.io.File?, size: Int, onTake: () -> Unit, onPick: () -> Unit, onRemove: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Box(Modifier.size((size + 8).dp)) {
        Avatar(name, size, photo)
        Surface(
            onClick = { showDialog = true }, shape = CircleShape, color = RColors.Blue,
            modifier = Modifier.align(Alignment.BottomEnd).size(34.dp), shadowElevation = 2.dp
        ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CameraAlt, "Cambiar foto", tint = Color.White, modifier = Modifier.size(18.dp)) } }
    }
    if (showDialog) {
        PhotoSourceDialog(
            onDismiss = { showDialog = false }, onTake = onTake, onPick = onPick,
            onRemove = if (photo != null) onRemove else null
        )
    }
}

@Composable
fun ProfileScreen(
    onTab: (String) -> Unit, onLogin: () -> Unit, onEditProfile: () -> Unit, onSchedule: () -> Unit,
    onTrips: () -> Unit, onReviews: () -> Unit, onPrivacy: () -> Unit, onDriver: (DriverStatus) -> Unit,
    onDisabled: () -> Unit, onLoggedOut: () -> Unit, vm: ProfileViewModel = hiltViewModel()
) {
    val s by vm.session.collectAsStateWithLifecycle()
    val stats by vm.passengerStats.collectAsStateWithLifecycle()
    val trips by vm.finishedTrips.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val actions = rememberPhotoActions { vm.setPhoto(it) }
    var showDelete by remember { mutableStateOf(false) }
    val user = s.user
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { PassengerBar(Routes.PROFILE, onTab) }) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Perfil", style = MaterialTheme.typography.headlineSmall)
            if (user == null) {
                InfoCard {
                    Text("Inicia sesión para ver tu perfil", style = MaterialTheme.typography.titleMedium)
                    Text("Guarda tus rutas, reservas y horario.", color = RColors.Muted)
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Iniciar sesión", onLogin)
                }
                return@Column
            }
            InfoCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfilePhotoHeader(user.name, s.profilePhoto, 96, actions.take, actions.pick, vm::removePhoto)
                    Spacer(Modifier.height(8.dp))
                    Text(user.name, style = MaterialTheme.typography.titleLarge)
                    Text(user.university, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    Text(user.email, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TagChip(user.type.label, RColors.BlueSoft, RColors.DarkBlue)
                        TagChip("Turno ${s.shift.label}", RColors.Yellow, RColors.Text)
                        s.zone?.let { TagChip(it, RColors.BlueSoft, RColors.DarkBlue) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatBlock(if (stats.second == 0) "–" else "⭐ ${stats.first}", "Calificación")
                        StatBlock("$trips", "Viajes")
                        StatBlock("${stats.second}", "Reseñas")
                    }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("Editar perfil", onEditProfile)
                }
            }
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)
            InfoCard {
                ProfileRow(Icons.Filled.Person, "Editar perfil", onEditProfile)
                ProfileRow(Icons.Filled.Schedule, "Mi horario (${s.entryTime} – ${s.exitTime})", onSchedule)
                ProfileRow(Icons.Filled.DirectionsCar, "Mis viajes", onTrips)
                ProfileRow(Icons.Filled.Star, "Reseñas y calificaciones", onReviews)
                ProfileRow(Icons.Filled.Lock, "Privacidad", onPrivacy)
            }
            InfoCard(color = RColors.CoralSoft) {
                Text("¿Tienes automóvil?", style = MaterialTheme.typography.titleMedium)
                Text("Comparte tu ruta con la comunidad y recupera gastos de gasolina.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    when (s.driverStatus) {
                        DriverStatus.APPROVED -> "Ir al modo conductor"
                        DriverStatus.IN_REVIEW -> "Ver estado de mi registro"
                        DriverStatus.IN_PROGRESS -> "Continuar registro"
                        DriverStatus.NONE -> "Quiero ser conductor"
                    },
                    onClick = { onDriver(s.driverStatus) }, color = RColors.Coral
                )
            }
            InfoCard {
                Text("Opciones demo", style = MaterialTheme.typography.titleMedium)
                Text("Simula una cuenta inhabilitada:", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                ChipGroup(DemoData.disabledReasons, emptySet()) { reason -> vm.disable(reason); onDisabled() }
                TextButton(onClick = { vm.resetDemo(); Toast.makeText(context, "Datos demo restablecidos", Toast.LENGTH_SHORT).show() }) {
                    Text("Restablecer datos demo")
                }
            }
            TextButton(onClick = { vm.logout(); onLoggedOut() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(6.dp)); Text("Cerrar sesión")
            }
            TextButton(onClick = { showDelete = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Filled.Delete, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp))
                Text("Eliminar cuenta", color = RColors.Coral)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (showDelete) {
        DeleteAccountDialog(onDismiss = { showDelete = false }, onConfirm = {
            showDelete = false
            vm.deleteAccount {
                Toast.makeText(context, "Tu cuenta fue eliminada", Toast.LENGTH_LONG).show()
                onLoggedOut()
            }
        })
    }
}

@Composable
fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = RColors.Blue)
        Spacer(Modifier.width(14.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = RColors.Muted)
    }
}

/** Editar perfil: foto, nombre, correo, tipo de usuario, zona y horario. */
@Composable
fun EditProfileScreen(onBack: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val actions = rememberPhotoActions { vm.setPhoto(it) }
    val user = s.user
    var name by rememberSaveable { mutableStateOf(user?.name.orEmpty()) }
    var email by rememberSaveable { mutableStateOf(user?.email.orEmpty()) }
    var type by rememberSaveable { mutableStateOf((user?.type ?: UserType.ALUMNO).name) }
    var zone by rememberSaveable { mutableStateOf(s.zone) }
    var shift by rememberSaveable { mutableStateOf(s.shift.name) }
    var entry by rememberSaveable { mutableStateOf(s.entryTime) }
    var exit by rememberSaveable { mutableStateOf(s.exitTime) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val isDemo = (user?.id ?: 0L) <= 0L

    Scaffold(topBar = { RutitasTopBar("Editar perfil", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ProfilePhotoHeader(name.ifBlank { "?" }, s.profilePhoto, 96, actions.take, actions.pick, vm::removePhoto)
                Text("Toca la cámara para cambiar tu foto", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            RutitasField(name, { name = it; error = null }, "Nombre", error = if (name.isBlank()) "El nombre no puede estar vacío" else null)
            RutitasField(email, { email = it; error = null }, "Correo institucional", keyboardType = KeyboardType.Email, enabled = !isDemo)
            if (isDemo) Text("El correo de las cuentas demo no se puede cambiar.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            Text("Tipo de usuario", style = MaterialTheme.typography.titleMedium)
            ChipGroup(UserType.entries.map { it.label }, setOf(UserType.valueOf(type).label)) { label ->
                type = UserType.entries.first { it.label == label }.name
            }
            Text("Zona", style = MaterialTheme.typography.titleMedium)
            ChipGroup(DemoData.zones, setOfNotNull(zone)) { zone = it }
            Text("Horario", style = MaterialTheme.typography.titleMedium)
            ChipGroup(Shift.entries.map { it.label }, setOf(Shift.valueOf(shift).label)) { label ->
                val sh = Shift.entries.first { it.label == label }
                shift = sh.name; entry = sh.entry; exit = sh.exit
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeField(entry, { entry = it }, "Hora de entrada", Modifier.weight(1f))
                TimeField(exit, { exit = it }, "Hora de salida", Modifier.weight(1f))
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(4.dp))
            PrimaryButton("Guardar cambios", loading = saving, enabled = name.isNotBlank(), onClick = {
                saving = true; error = null
                vm.updateProfile(name, email, UserType.valueOf(type), zone, Shift.valueOf(shift), entry, exit) { res ->
                    saving = false
                    res.onSuccess {
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        onBack()
                    }.onFailure { error = it.message }
                }
            })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DisabledAccountScreen(onBack: () -> Unit, onHome: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.session.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Estado de la cuenta", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!s.isDisabled) {
                Icon(Icons.Filled.CheckCircle, null, tint = RColors.Blue, modifier = Modifier.size(64.dp))
                Text("Tu cuenta está activa", style = MaterialTheme.typography.headlineSmall)
                PrimaryButton("Volver al mapa", onHome)
                return@Column
            }
            Surface(shape = CircleShape, color = RColors.CoralSoft, modifier = Modifier.size(88.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Warning, null, tint = RColors.Coral, modifier = Modifier.size(48.dp)) }
            }
            Text("Cuenta inhabilitada", style = MaterialTheme.typography.headlineSmall)
            InfoCard {
                Text("Motivo", color = RColors.Muted, style = MaterialTheme.typography.labelMedium)
                Text(s.disabledReason ?: "Verificación pendiente", style = MaterialTheme.typography.titleMedium)
            }
            InfoCard {
                Text("Mientras tu cuenta esté inhabilitada no puedes:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("• Reservar rutas\n• Publicar rutas\n• Aceptar solicitudes")
            }
            if (s.reviewRequested) {
                InfoCard(color = RColors.BlueSoft) { Text("Revisión solicitada. Te avisaremos cuando termine.") }
            } else {
                PrimaryButton("Solicitar revisión", onClick = { vm.requestReview() })
            }
            SecondaryButton("Volver al mapa", onHome)
            TextButton(onClick = { vm.enable() }) { Text("Reactivar cuenta (demo)") }
        }
    }
}
