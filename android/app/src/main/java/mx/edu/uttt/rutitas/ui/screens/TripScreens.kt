package mx.edu.uttt.rutitas.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import mx.edu.uttt.rutitas.model.Booking
import mx.edu.uttt.rutitas.model.BookingStatus
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.utils.money
import mx.edu.uttt.rutitas.viewmodel.BookingViewModel
import mx.edu.uttt.rutitas.viewmodel.TripsViewModel

@Composable
fun TripsScreen(
    onTab: (String) -> Unit, onTracking: (Long) -> Unit, onRate: (Long) -> Unit,
    onLogin: () -> Unit, onAlternatives: (Long) -> Unit, vm: TripsViewModel = hiltViewModel()
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val bookings by vm.bookings.collectAsStateWithLifecycle()
    val interrupted by vm.interrupted.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var codeFor by remember { mutableStateOf<Booking?>(null) }
    val upcoming = bookings.filter { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.PENDING }
    val history = bookings.filter { it.status == BookingStatus.FINISHED || it.status == BookingStatus.CANCELLED }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { PassengerBar(Routes.TRIPS, onTab) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Mis viajes", style = MaterialTheme.typography.headlineSmall)
            if (session.user == null) {
                Text("Inicia sesión para ver tus viajes y reservas.", color = RColors.Muted)
                PrimaryButton("Iniciar sesión", onLogin)
                return@Column
            }
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Próximos (${upcoming.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Historial") })
            }
            interrupted.firstOrNull()?.let { b ->
                InfoCard(color = RColors.CoralSoft) {
                    Text("Tu ruta ${b.title} fue interrumpida", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text("Te avisamos para que elijas otra opción.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onAlternatives(b.routeId) }) { Text("Ver alternativas") }
                }
            }
            val list = if (tab == 0) upcoming else history
            if (list.isEmpty()) {
                Text(
                    if (tab == 0) "No tienes viajes próximos. Reserva una ruta desde el mapa." else "Aún no tienes viajes en el historial.",
                    color = RColors.Muted
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list, key = { it.id }) { b ->
                    BookingCard(
                        b,
                        onView = { onTracking(b.id) }, onRate = { onRate(b.id) },
                        onCancel = { vm.cancel(b.id) }, onConfirm = { vm.confirm(b.id) },
                        onCode = { codeFor = b }
                    )
                }
            }
        }
    }
    codeFor?.let { CodeDialog(it.code) { codeFor = null } }
}

@Composable
private fun BookingCard(
    b: Booking, onView: () -> Unit, onRate: () -> Unit, onCancel: () -> Unit, onConfirm: () -> Unit, onCode: () -> Unit
) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(b.title, style = MaterialTheme.typography.titleMedium)
                Text("${b.dateLabel} · ${b.departure}", color = RColors.Muted)
            }
            StatusChip(b.status.label)
        }
        Spacer(Modifier.height(6.dp))
        Text("Punto: ${b.pickup}", style = MaterialTheme.typography.bodySmall, color = RColors.Muted)
        Text("${b.shift.label} · ${b.direction.label}", style = MaterialTheme.typography.bodySmall, color = RColors.Muted)
        Text("${b.driverName} · ${b.car}", style = MaterialTheme.typography.bodySmall, color = RColors.Muted)
        Text("${b.code} · ${money(b.total)}", style = MaterialTheme.typography.bodySmall, color = RColors.Muted)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (b.status) {
                BookingStatus.CONFIRMED -> {
                    Button(onClick = onView, shape = RoundedCornerShape(12.dp)) { Text("Ver viaje") }
                    OutlinedButton(onClick = onCode, shape = RoundedCornerShape(12.dp)) { Text("Ver código") }
                    TextButton(onClick = onCancel) { Text("Cancelar", color = RColors.Coral) }
                }
                BookingStatus.PENDING -> {
                    OutlinedButton(onClick = onConfirm, shape = RoundedCornerShape(12.dp)) { Text("Simular confirmación") }
                    TextButton(onClick = onCancel) { Text("Cancelar", color = RColors.Coral) }
                }
                BookingStatus.FINISHED -> if (!b.rated) {
                    Button(onClick = onRate, shape = RoundedCornerShape(12.dp)) { Text("Calificar") }
                } else Text("Calificado ⭐", color = RColors.Muted)
                BookingStatus.CANCELLED -> Text("Reserva cancelada", color = RColors.Muted)
            }
        }
    }
}

@Composable
fun TrackingScreen(onBack: () -> Unit, onStartTrip: (Long) -> Unit, vm: BookingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val booking by vm.booking.collectAsStateWithLifecycle()
    val route by vm.route.collectAsStateWithLifecycle()
    var eta by rememberSaveable { mutableIntStateOf(5) }
    var showMessage by remember { mutableStateOf(false) }
    var showCode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(eta) { if (eta > 0) { delay(2000); eta -= 1 } }

    Scaffold(topBar = { RutitasTopBar("Seguimiento", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        val b = booking
        if (b == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RutitasMap(listOfNotNull(route), Modifier.fillMaxWidth().height(230.dp), highlight = route?.id, driverProgress = (5 - eta) / 5f * 0.35f)
            InfoCard {
                Text(if (eta > 0) "Tu conductor está en camino" else "Tu conductor llegó", style = MaterialTheme.typography.titleLarge)
                Text(if (eta > 0) "Llega en $eta min" else "Te espera en el punto de encuentro", color = RColors.Blue, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
            val waitLeft = waitRemainingSeconds(route?.waitStartedAt ?: 0L, route?.waitMinutes ?: 5)
            InfoCard(color = if (waitLeft != null) RColors.BlueSoft else Color.White) {
                if (waitLeft != null) {
                    Text("Tu conductor te está esperando", style = MaterialTheme.typography.titleMedium, color = RColors.DarkBlue)
                    Text(
                        if (waitLeft > 0) "Tiempo de espera restante: ${formatMmSs(waitLeft)}" else "El tiempo de espera terminó. ¡Apúrate al punto de encuentro!",
                        color = RColors.Text
                    )
                } else {
                    Text("Tiempo de espera", color = RColors.Muted, style = MaterialTheme.typography.labelMedium)
                    Text("El conductor esperará hasta ${route?.waitMinutes ?: 5} min en tu punto de encuentro.")
                }
            }
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(b.driverName)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(b.driverName, style = MaterialTheme.typography.titleMedium)
                        Text(b.car, color = RColors.Muted)
                    }
                    Text("⭐ ${route?.driverRating ?: 4.8}")
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text("Punto de encuentro", color = RColors.Muted, style = MaterialTheme.typography.labelMedium)
                Text(b.pickup, style = MaterialTheme.typography.titleMedium)
            }
            if (eta <= 2) {
                InfoCard(color = RColors.BlueSoft) {
                    Text(if (eta > 0) "Tu conductor está por llegar" else "Tu conductor ya llegó", style = MaterialTheme.typography.titleMedium, color = RColors.DarkBlue)
                    Text("Muéstrale tu código para abordar:", color = RColors.Muted)
                    Text(b.code, style = MaterialTheme.typography.headlineMedium, color = RColors.Text)
                }
            }
            SecondaryButton("Ver mi código", onClick = { showCode = true })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showMessage = true }, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Filled.Chat, null); Spacer(Modifier.width(6.dp)); Text("Mensaje")
                }
                OutlinedButton(
                    onClick = {
                        try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:7710000000"))) }
                        catch (e: ActivityNotFoundException) { Toast.makeText(context, "Este dispositivo no puede hacer llamadas.", Toast.LENGTH_SHORT).show() }
                    },
                    Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)
                ) { Icon(Icons.Filled.Phone, null); Spacer(Modifier.width(6.dp)); Text("Llamar") }
            }
            PrimaryButton("Iniciar viaje", enabled = eta == 0, onClick = { vm.startTrip(); onStartTrip(b.id) })
            if (eta > 0) TextButton(onClick = { eta = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Saltar espera (demo)") }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showCode) booking?.let { CodeDialog(it.code) { showCode = false } }

    if (showMessage) {
        AlertDialog(
            onDismissRequest = { showMessage = false },
            title = { Text("Mensaje al conductor") },
            text = { OutlinedTextField(message, { message = it }, placeholder = { Text("Ya voy para allá") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (message.isNotBlank()) {
                        Toast.makeText(context, "Mensaje enviado (demo)", Toast.LENGTH_SHORT).show()
                        message = ""; showMessage = false
                    }
                }) { Text("Enviar") }
            },
            dismissButton = { TextButton(onClick = { showMessage = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun TripInProgressScreen(
    onBack: () -> Unit, onReport: (Long) -> Unit, onFinish: (Long) -> Unit, onHome: () -> Unit,
    vm: BookingViewModel = hiltViewModel()
) {
    val booking by vm.booking.collectAsStateWithLifecycle()
    val route by vm.route.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Viaje en curso", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        val b = booking
        val r = route
        if (b == null || r == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        val stages = r.tripStages()
        val p = b.progress.coerceIn(0, stages.size)
        if (p >= stages.size) androidx.activity.compose.BackHandler { onHome() }
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RutitasMap(listOf(r), Modifier.fillMaxWidth().height(200.dp), highlight = r.id, driverProgress = p / stages.size.toFloat())
            Text(r.title, style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(progress = { p / stages.size.toFloat() }, Modifier.fillMaxWidth(), color = RColors.Blue)
            Text("$p de ${stages.size} puntos completados", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            InfoCard {
                stages.forEachIndexed { i, s ->
                    val state = when { i < p -> 0; i == p -> 1; else -> 2 }
                    val label = when (state) { 0 -> "Completado"; 1 -> "En camino"; else -> "Pendiente" }
                    TimelineRow(s, label, state, i == stages.lastIndex)
                }
            }
            if (p < stages.size) {
                PrimaryButton("Avanzar recorrido (demo)", onClick = { vm.advance(stages.size) })
                TextButton(onClick = { onReport(r.id) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Filled.ReportProblem, null, tint = RColors.Coral)
                    Spacer(Modifier.width(6.dp))
                    Text("Reportar problema", color = RColors.Coral)
                }
            } else {
                InfoCard(color = RColors.BlueSoft) { Text("¡Llegaste a UTTT! Tu viaje terminó.") }
                PrimaryButton("Calificar viaje", onClick = { onFinish(b.id) })
                SecondaryButton("Volver al mapa", onClick = onHome)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun RatingScreen(onDone: () -> Unit, vm: BookingViewModel = hiltViewModel()) {
    val booking by vm.booking.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var stars by rememberSaveable { mutableIntStateOf(5) }
    var tags by rememberSaveable { mutableStateOf(setOf<String>()) }
    var comment by rememberSaveable { mutableStateOf("") }
    androidx.activity.compose.BackHandler { onDone() }
    val options = listOf("Puntual", "Amable", "Manejo seguro", "Auto limpio", "Buena comunicación")
    Scaffold(topBar = { RutitasTopBar("¿Cómo estuvo tu viaje?") }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            booking?.let {
                Avatar(it.driverName, 64)
                Text(it.driverName, style = MaterialTheme.typography.titleMedium)
                Text(it.title, color = RColors.Muted)
            }
            StarRow(stars, { stars = it })
            Text("¿Qué te gustó?", Modifier.align(Alignment.Start), style = MaterialTheme.typography.titleMedium)
            ChipGroup(options, tags) { tags = if (it in tags) tags - it else tags + it }
            OutlinedTextField(
                comment, { comment = it }, Modifier.fillMaxWidth().height(110.dp),
                label = { Text("Comentario (opcional)") }, shape = RoundedCornerShape(14.dp)
            )
            PrimaryButton("Enviar calificación", onClick = {
                vm.rate(stars, tags, comment) {
                    Toast.makeText(context, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show()
                    onDone()
                }
            })
            TextButton(onClick = onDone) { Text("Calificar después") }
        }
    }
}
