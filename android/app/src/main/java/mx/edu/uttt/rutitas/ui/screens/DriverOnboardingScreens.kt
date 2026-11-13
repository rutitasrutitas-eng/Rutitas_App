package mx.edu.uttt.rutitas.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import mx.edu.uttt.rutitas.model.DocKind
import mx.edu.uttt.rutitas.model.DocStatus
import mx.edu.uttt.rutitas.model.Vehicle
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.DriverOnboardingViewModel

@Composable
fun DriverIntroScreen(onBack: () -> Unit, onStart: () -> Unit, vm: DriverOnboardingViewModel = hiltViewModel()) {
    Scaffold(topBar = { RutitasTopBar("Quiero ser conductor", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(color = RColors.BlueSoft, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.DirectionsCar, null, tint = RColors.Blue, modifier = Modifier.size(80.dp)) }
            }
            Text("Conduce con Rutitas", style = MaterialTheme.typography.headlineMedium)
            Text("Comparte tu trayecto con la comunidad UTTT.", color = RColors.Muted)
            InfoCard {
                BenefitRow(Icons.Filled.AltRoute, "Comparte rutas", "Publica tu trayecto diario a la universidad.")
                BenefitRow(Icons.Filled.People, "Ayuda a tu comunidad", "Lleva a compañeros que viven cerca de ti.")
                BenefitRow(Icons.Filled.AttachMoney, "Recupera tus gastos", "Reduce el costo de la gasolina.")
            }
            PrimaryButton("Comenzar registro", onClick = { vm.start(); onStart() })
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Ahora no") }
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, title: String, text: String) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = RColors.Blue)
        Spacer(Modifier.width(14.dp))
        Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(text, color = RColors.Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

private data class PermSpec(val title: String, val why: String, val icon: ImageVector, val perms: List<String>)

private fun specs() = listOf(
    PermSpec("Cámara", "Para tomar la foto de tu credencial y de tu vehículo.", Icons.Filled.CameraAlt, listOf(Manifest.permission.CAMERA)),
    // El selector de fotos de Android funciona sin permisos de galería.
    PermSpec("Fotos", "Selecciona imágenes con el selector privado del sistema; no requiere permiso.", Icons.Filled.PhotoLibrary, emptyList()),
    PermSpec(
        "Ubicación", "Para mostrar tu recorrido y que los pasajeros te encuentren.", Icons.Filled.LocationOn,
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
)

@Composable
fun DriverPermissionsScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val context = LocalContext.current
    val all = remember { specs() }
    var refresh by remember { mutableIntStateOf(0) }
    var asked by remember { mutableStateOf(setOf<String>()) }
    var pendingTitles by remember { mutableStateOf(setOf<String>()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        asked = asked + pendingTitles
        refresh++
    }

    fun granted(spec: PermSpec): Boolean = spec.perms.isEmpty() || spec.perms.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val tick = refresh
    val anyDenied = tick >= 0 && all.any { !granted(it) && it.title in asked }

    Scaffold(topBar = { RutitasTopBar("Permisos", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Permisos necesarios", style = MaterialTheme.typography.headlineMedium)
            Text("Te explicamos para qué usamos cada uno.", color = RColors.Muted)
            all.forEach { spec ->
                val ok = granted(spec)
                val label = when { ok -> "Concedido"; spec.title in asked -> "Rechazado"; else -> "Pendiente" }
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(spec.icon, null, tint = RColors.Blue)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(spec.title, style = MaterialTheme.typography.titleMedium)
                            Text(spec.why, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (ok) StatusChip(if (spec.perms.isEmpty()) "Completado" else label)
                        else TextButton(onClick = {
                            pendingTitles = setOf(spec.title)
                            launcher.launch(spec.perms.toTypedArray())
                        }) { Text("Permitir") }
                    }
                }
            }
            val missing = all.filter { !granted(it) }
            if (missing.isNotEmpty()) {
                SecondaryButton("Permitir todos", onClick = {
                    pendingTitles = missing.map { it.title }.toSet()
                    launcher.launch(missing.flatMap { it.perms }.distinct().toTypedArray())
                })
            }
            if (anyDenied) {
                InfoCard(color = RColors.CoralSoft) {
                    Text("Algunos permisos fueron rechazados. En modo demo puedes continuar; las funciones relacionadas usarán la galería o datos simulados.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                    }) { Text("Abrir ajustes") }
                }
            }
            PrimaryButton("Continuar", onClick = onContinue)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DriverIdentityScreen(onBack: () -> Unit, onContinue: () -> Unit, vm: DriverOnboardingViewModel = hiltViewModel()) {
    SecureScreen() // bloquea capturas de pantalla mientras se ve la credencial
    val photos by vm.photos.collectAsStateWithLifecycle()
    var consent by rememberSaveable { mutableStateOf(false) }
    val actions = rememberPhotoActions { vm.setPhoto("identity", it) }
    Scaffold(topBar = { RutitasTopBar("Verifica tu identidad", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Credencial institucional", style = MaterialTheme.typography.headlineSmall)
            Text("Toma una foto clara de tu credencial o elige una imagen.", color = RColors.Muted)
            InfoCard(color = RColors.BlueSoft) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, null, tint = RColors.Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("Así protegemos tu foto", style = MaterialTheme.typography.titleMedium, color = RColors.DarkBlue)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "• Se guarda solo en tu teléfono, en almacenamiento privado de Rutitas (no aparece en la galería).\n" +
                        "• Quitamos los datos ocultos de la imagen: ubicación GPS y modelo del teléfono.\n" +
                        "• Esta pantalla bloquea capturas y grabaciones de pantalla.\n" +
                        "• Solo se usa para verificar tu identidad. Puedes borrarla cuando quieras y se elimina al borrar tu cuenta.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("Consejos: evita reflejos, que se lean tus datos y no hay nadie más en la foto.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it })
                Text("Acepto que Rutitas use mi foto solo para verificar mi identidad.", style = MaterialTheme.typography.bodyMedium)
            }
            PhotoBox(photos["identity"], "Sin foto todavía", Icons.Filled.Badge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = actions.take, enabled = consent, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Filled.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Tomar foto")
                }
                OutlinedButton(onClick = actions.pick, enabled = consent, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Filled.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Elegir imagen")
                }
            }
            if (photos["identity"] != null) {
                TextButton(onClick = { vm.removePhoto("identity") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Filled.Delete, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp))
                    Text("Eliminar foto", color = RColors.Coral)
                }
            }
            PrimaryButton("Continuar", enabled = photos["identity"] != null && consent, onClick = onContinue)
            if (!consent) Text("Acepta el uso de tu foto para poder tomarla.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DriverVehicleScreen(onBack: () -> Unit, onContinue: () -> Unit, vm: DriverOnboardingViewModel = hiltViewModel()) {
    val session by vm.session.collectAsStateWithLifecycle()
    val photos by vm.photos.collectAsStateWithLifecycle()
    val v = session.vehicle
    var brand by rememberSaveable { mutableStateOf(v.brand) }
    var model by rememberSaveable { mutableStateOf(v.model) }
    var year by rememberSaveable { mutableStateOf(v.year) }
    var color by rememberSaveable { mutableStateOf(v.color) }
    var plates by rememberSaveable { mutableStateOf(v.plates) }
    var seats by rememberSaveable { mutableIntStateOf(v.seats) }
    var tried by remember { mutableStateOf(false) }
    val actions = rememberPhotoActions { vm.setPhoto("vehicle", it) }

    val yearNum = year.toIntOrNull()
    fun req(s: String) = if (tried && s.isBlank()) "Obligatorio" else null

    Scaffold(topBar = { RutitasTopBar("Tu vehículo", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoBox(photos["vehicle"], "Foto del vehículo (opcional)", Icons.Filled.DirectionsCar)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = actions.take, Modifier.weight(1f)) { Text("Tomar foto") }
                OutlinedButton(onClick = actions.pick, Modifier.weight(1f)) { Text("Elegir imagen") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RutitasField(brand, { brand = it }, "Marca", Modifier.weight(1f), error = req(brand))
                RutitasField(model, { model = it }, "Modelo", Modifier.weight(1f), error = req(model))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RutitasField(year, { year = it.filter(Char::isDigit).take(4) }, "Año", Modifier.weight(1f), KeyboardType.Number,
                    error = if (tried && (yearNum == null || yearNum !in 1990..2027)) "Año no válido" else null)
                RutitasField(color, { color = it }, "Color", Modifier.weight(1f), error = req(color))
            }
            RutitasField(plates, { plates = it.uppercase() }, "Placas", error = if (tried && plates.trim().length < 5) "Mínimo 5 caracteres" else null)
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lugares disponibles", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (seats > 1) seats-- }) { Icon(Icons.Filled.Remove, "Menos") }
                    Text("$seats", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { if (seats < 6) seats++ }) { Icon(Icons.Filled.Add, "Más") }
                }
            }
            TextButton(onClick = { brand = "Nissan"; model = "Versa"; year = "2022"; color = "Gris"; plates = "ABC-123-D"; seats = 3 }) {
                Text("Autocompletar datos demo")
            }
            PrimaryButton("Continuar", onClick = {
                tried = true
                if (brand.isNotBlank() && model.isNotBlank() && color.isNotBlank() && plates.trim().length >= 5 && yearNum != null && yearNum in 1990..2027) {
                    vm.saveVehicle(Vehicle(brand.trim(), model.trim(), year, color.trim(), plates.trim(), seats))
                    onContinue()
                }
            })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DriverDocumentsScreen(onBack: () -> Unit, onContinue: () -> Unit, vm: DriverOnboardingViewModel = hiltViewModel()) {
    SecureScreen() // documentos sensibles: sin capturas de pantalla
    val session by vm.session.collectAsStateWithLifecycle()
    val allDone = session.documents.values.all { it == DocStatus.COMPLETED }
    Scaffold(topBar = { RutitasTopBar("Documentos", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tus documentos", style = MaterialTheme.typography.headlineSmall)
            Text("Sube una foto de cada documento. Se guardan en almacenamiento privado, sin datos de ubicación, y no se pueden capturar con pantallazo.", color = RColors.Muted)
            DocKind.entries.forEach { kind ->
                val status = session.documents[kind] ?: DocStatus.PENDING
                val actions = rememberPhotoActions { vm.setDoc(kind, it) }
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, null, tint = RColors.Blue)
                        Spacer(Modifier.width(10.dp))
                        Text(kind.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        StatusChip(status.label)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = actions.take) { Text("Tomar foto") }
                        TextButton(onClick = actions.pick) { Text(if (status == DocStatus.COMPLETED) "Reemplazar" else "Elegir imagen") }
                    }
                }
            }
            TextButton(onClick = { vm.completeAllDocs() }) { Text("Autocompletar documentos (demo)") }
            PrimaryButton("Enviar a revisión", enabled = allDone, onClick = { vm.submit(); onContinue() })
            if (!allDone) Text("Completa los tres documentos para continuar.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DriverReviewScreen(onApproved: () -> Unit, vm: DriverOnboardingViewModel = hiltViewModel()) {
    val session by vm.session.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { delay(3000); vm.approve(); onApproved() }
    Scaffold(topBar = { RutitasTopBar("En revisión") }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
            Text("Estamos revisando tu registro", style = MaterialTheme.typography.headlineSmall)
            Text("Esto toma unos segundos en modo demo.", color = RColors.Muted)
            InfoCard {
                session.documents.forEach { (kind, st) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(kind.label, Modifier.weight(1f)); StatusChip(st.label)
                    }
                }
            }
            SecondaryButton("Aprobar ahora (demo)", onClick = { vm.approve(); onApproved() })
        }
    }
}

@Composable
fun DriverApprovedScreen(onGo: () -> Unit) {
    Scaffold(topBar = { RutitasTopBar("Registro aprobado") }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Surface(shape = CircleShape, color = RColors.BlueSoft, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CheckCircle, null, tint = RColors.Blue, modifier = Modifier.size(56.dp)) }
            }
            Text("¡Ya puedes publicar rutas!", style = MaterialTheme.typography.headlineSmall)
            Text("Tu perfil de conductor fue aprobado.", color = RColors.Muted)
            PrimaryButton("Ir al modo conductor", onGo)
        }
    }
}
