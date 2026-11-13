package mx.edu.uttt.rutitas.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.UserType
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.utils.AppConfig
import mx.edu.uttt.rutitas.utils.TimeUtils
import mx.edu.uttt.rutitas.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onBack: () -> Unit, onSuccess: () -> Unit, onDisabled: () -> Unit, onRegister: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    Scaffold(topBar = { RutitasTopBar("Iniciar sesión", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Entra a Rutitas", style = MaterialTheme.typography.headlineMedium)
            Text("Comparte rutas seguras con tu comunidad UTTT.", color = RColors.Muted)
            RutitasField(
                email, { email = it; vm.clearError() }, "Correo", keyboardType = KeyboardType.Email,
                error = if (tried && email.isBlank()) "Escribe tu correo" else null
            )
            RutitasField(
                pass, { pass = it; vm.clearError() }, "Contraseña", password = true,
                error = if (tried && pass.isBlank()) "Escribe tu contraseña" else null
            )
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (ui.error?.contains("eliminada") == true) {
                SecondaryButton("Restaurar cuentas demo", onClick = { vm.restoreDemo() })
            }
            PrimaryButton("Iniciar sesión", loading = ui.loading, onClick = {
                tried = true
                if (email.isNotBlank() && pass.isNotBlank()) {
                    vm.login(email, pass) { disabled -> if (disabled) onDisabled() else onSuccess() }
                }
            })
            TextButton(onClick = { email = DemoData.DEMO_EMAIL; pass = DemoData.DEMO_PASSWORD }) {
                Text("Usar cuenta demo (demo@rutitas.com)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?", color = RColors.Muted)
                TextButton(onClick = onRegister) { Text("Crear cuenta") }
            }
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onSuccess: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(UserType.ALUMNO.name) }
    var tried by remember { mutableStateOf(false) }

    val nameErr = if (tried && name.isBlank()) "Escribe tu nombre" else null
    val emailErr = when {
        !tried -> null
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido"
        !AppConfig.isInstitutionalEmail(email) -> "Usa el correo de tu institución (ej. nombre@uttt.edu.mx)"
        else -> null
    }
    val passErr = if (tried && pass.length < 6) "Mínimo 6 caracteres" else null
    val confErr = if (tried && confirm != pass) "Las contraseñas no coinciden" else null

    Scaffold(topBar = { RutitasTopBar("Registro", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium)
            RutitasField(name, { name = it }, "Nombre", error = nameErr)
            RutitasField(email, { email = it; vm.clearError() }, "Correo institucional", keyboardType = KeyboardType.Email, error = emailErr)
            if (emailErr == null) Text("Dominios válidos: ${AppConfig.ALLOWED_DOMAINS.joinToString(", ")}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            RutitasField(pass, { pass = it }, "Contraseña", password = true, error = passErr)
            RutitasField(confirm, { confirm = it }, "Confirmar contraseña", password = true, error = confErr)
            Text("Soy", style = MaterialTheme.typography.titleMedium)
            ChipGroup(UserType.entries.map { it.label }, setOf(UserType.valueOf(type).label)) { label ->
                type = UserType.entries.first { it.label == label }.name
            }
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(4.dp))
            PrimaryButton("Crear cuenta", loading = ui.loading, onClick = {
                tried = true
                if (name.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                    AppConfig.isInstitutionalEmail(email) && pass.length >= 6 && confirm == pass
                ) vm.register(name, email, pass, UserType.valueOf(type), onSuccess)
            })
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LocationScreen(onBack: () -> Unit, onContinue: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val session by vm.session.collectAsStateWithLifecycle()
    var zone by rememberSaveable { mutableStateOf(session.zone) }
    var query by rememberSaveable { mutableStateOf("") }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            zone = "Tula Centro"; vm.setZone("Tula Centro")
            Toast.makeText(context, "Ubicación detectada. Zona más cercana: Tula Centro (demo)", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Sin permiso de ubicación. Elige tu zona manualmente.", Toast.LENGTH_LONG).show()
        }
    }
    val zones = DemoData.zones.filter { query.isBlank() || it.contains(query, true) }
    Scaffold(topBar = { RutitasTopBar("Ubicación", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("¿Desde dónde viajas?", style = MaterialTheme.typography.headlineMedium)
            Text("Así te mostramos las rutas más cercanas a ti.", color = RColors.Muted)
            RutitasMap(routes = emptyList(), modifier = Modifier.fillMaxWidth().height(160.dp))
            SecondaryButton("Usar mi ubicación", onClick = { permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) })
            OutlinedTextField(
                query, { query = it }, Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Buscar zona") }, leadingIcon = { Icon(Icons.Filled.Search, null) },
                shape = RoundedCornerShape(14.dp)
            )
            if (zones.isEmpty()) Text("No encontramos esa zona.", color = RColors.Muted)
            ChipGroup(zones, setOfNotNull(zone)) { zone = it; vm.setZone(it) }
            PrimaryButton("Continuar", enabled = zone != null, onClick = onContinue)
            if (zone == null) Text("Elige una zona para continuar.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ScheduleScreen(edit: Boolean, onBack: () -> Unit, onContinue: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val session by vm.session.collectAsStateWithLifecycle()
    var shift by rememberSaveable { mutableStateOf(session.shift.name) }
    var entry by rememberSaveable { mutableStateOf(session.entryTime) }
    var exit by rememberSaveable { mutableStateOf(session.exitTime) }
    var tried by remember { mutableStateOf(false) }
    Scaffold(topBar = { RutitasTopBar("Horario", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tu horario", style = MaterialTheme.typography.headlineMedium)
            Text("Lo usamos para ordenar las rutas que mejor te convienen.", color = RColors.Muted)
            Shift.entries.forEach { s ->
                val selected = shift == s.name
                Card(
                    onClick = { shift = s.name; entry = s.entry; exit = s.exit },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selected) RColors.BlueSoft else Color.White),
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) RColors.Blue else RColors.Line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected, onClick = { shift = s.name; entry = s.entry; exit = s.exit })
                        Column {
                            Text(s.label, style = MaterialTheme.typography.titleMedium)
                            Text(s.range, color = RColors.Muted)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeField(entry, { entry = it }, "Hora de entrada", Modifier.weight(1f))
                TimeField(exit, { exit = it }, "Hora de salida", Modifier.weight(1f))
            }
            PrimaryButton(if (edit) "Guardar" else "Continuar", onClick = {
                tried = true
                if (TimeUtils.isValid(entry) && TimeUtils.isValid(exit)) {
                    vm.saveSchedule(Shift.valueOf(shift), TimeUtils.format(TimeUtils.toMinutes(entry)!!), TimeUtils.format(TimeUtils.toMinutes(exit)!!))
                    onContinue()
                }
            })
        }
    }
}

@Composable
fun PrivacyScreen(edit: Boolean, onBack: () -> Unit, onDone: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val session by vm.session.collectAsStateWithLifecycle()
    var accepted by rememberSaveable { mutableStateOf(session.privacyAccepted) }
    Scaffold(topBar = { RutitasTopBar("Privacidad", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = RColors.BlueSoft, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, null, tint = RColors.Blue, modifier = Modifier.size(48.dp)) }
            }
            Text("Tu información está protegida", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Usaremos tus datos para recomendar rutas y mejorar tu seguridad.",
                color = RColors.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text("Acepto el aviso de privacidad.")
            }
            PrimaryButton(if (edit) "Guardar" else "Entrar a Rutitas", enabled = accepted, onClick = {
                vm.acceptPrivacy(); onDone()
            })
        }
    }
}
