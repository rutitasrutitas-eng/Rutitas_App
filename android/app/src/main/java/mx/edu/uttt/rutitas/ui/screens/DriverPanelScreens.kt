package mx.edu.uttt.rutitas.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.RequestStatus
import mx.edu.uttt.rutitas.model.RideRequest
import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.utils.TimeUtils
import mx.edu.uttt.rutitas.utils.money
import mx.edu.uttt.rutitas.utils.seatsText
import mx.edu.uttt.rutitas.viewmodel.DriverViewModel

@Composable
fun DriverHomeScreen(
    onTab: (String) -> Unit, onPublish: () -> Unit, onOpenRoute: (Long) -> Unit, onRoutes: () -> Unit,
    onRequests: () -> Unit, onEarnings: () -> Unit, onReport: (Long) -> Unit, onDisabled: () -> Unit,
    vm: DriverViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val s by vm.session.collectAsStateWithLifecycle()
    val routes by vm.myRoutes.collectAsStateWithLifecycle()
    val requests by vm.requests.collectAsStateWithLifecycle()
    val next = routes.firstOrNull { it.isOpen }
    val pending = requests.count { it.status == RequestStatus.PENDING }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DriverBar(Routes.DRIVER_HOME, onTab) }) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Hola, ${s.user?.name?.substringBefore(' ') ?: "conductor"}", style = MaterialTheme.typography.headlineSmall)
            Text("Tu horario: ${s.shift.label} · ${s.entryTime} – ${s.exitTime}", color = RColors.Muted)
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)
            Surface(color = RColors.Blue, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Próxima ruta", color = Color.White.copy(alpha = 0.8f))
                    if (next != null) {
                        Text(next.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(next.departure, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("${seatsText(next.seats)} disponibles · espera ${next.waitMinutes} min", color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(2.dp))
                        StatusChip(next.status.label)
                    } else {
                        Text("Aún no tienes rutas publicadas", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (next != null && next.status == RouteStatus.ACTIVE) {
                        Button(
                            onClick = {
                                vm.startRoute(next.id) { err ->
                                    if (err != null) Toast.makeText(context, err, Toast.LENGTH_LONG).show() else onOpenRoute(next.id)
                                }
                            },
                            enabled = !s.isDisabled,
                            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RColors.Yellow, contentColor = RColors.Text)
                        ) {
                            Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp))
                            Text("Iniciar ruta", fontWeight = FontWeight.Bold)
                        }
                    } else if (next != null) {
                        Button(
                            onClick = { onOpenRoute(next.id) },
                            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RColors.Yellow, contentColor = RColors.Text)
                        ) { Text(if (next.status == RouteStatus.IN_PROGRESS) "Continuar ruta" else "Ver ruta", fontWeight = FontWeight.Bold) }
                    }
                    Button(
                        onClick = onPublish, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RColors.DarkBlue)
                    ) { Text("Publicar ruta", fontWeight = FontWeight.Bold) }
                    if (next != null) {
                        TextButton(onClick = { onOpenRoute(next.id) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Ver información completa", color = Color.White)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutCard("Mis rutas", Icons.Filled.AltRoute, null, Modifier.weight(1f), onRoutes)
                ShortcutCard("Solicitudes", Icons.Filled.People, pending.takeIf { it > 0 }?.toString(), Modifier.weight(1f), onRequests)
                ShortcutCard("Ganancias", Icons.Filled.AttachMoney, null, Modifier.weight(1f), onEarnings)
            }
            if (next != null && (next.status == RouteStatus.ACTIVE || next.status == RouteStatus.IN_PROGRESS)) {
                TextButton(onClick = { onReport(next.id) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Filled.ReportProblem, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp))
                    Text("Reportar problema", color = RColors.Coral)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ShortcutCard(label: String, icon: ImageVector, badge: String?, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BadgedBox(badge = { if (badge != null) Badge { Text(badge) } }) { Icon(icon, null, tint = RColors.Blue) }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DriverRoutesScreen(
    onTab: (String) -> Unit, onPublish: () -> Unit, onOpenRoute: (Long) -> Unit, onEdit: (Long) -> Unit,
    vm: DriverViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val routes by vm.myRoutes.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DriverBar(Routes.DRIVER_ROUTES, onTab) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onPublish, containerColor = RColors.Blue, contentColor = Color.White) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Publicar")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Mis rutas", style = MaterialTheme.typography.headlineSmall)
            if (routes.isEmpty()) Text("Aún no tienes rutas. Publica la primera.", color = RColors.Muted)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(routes, key = { it.id }) { r ->
                    Card(
                        onClick = { onOpenRoute(r.id) }, shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(r.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                StatusChip(r.status.label)
                            }
                            Text("${r.departure} · \$${r.price} · ${seatsText(r.seats)}", color = RColors.Muted)
                            Text(
                                (if (r.recurring) "Recurrente: ${r.days}" else r.days) + " · espera ${r.waitMinutes} min",
                                color = RColors.Muted, style = MaterialTheme.typography.bodySmall
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                when (r.status) {
                                    RouteStatus.ACTIVE -> Button(
                                        onClick = {
                                            vm.startRoute(r.id) { err ->
                                                if (err != null) Toast.makeText(context, err, Toast.LENGTH_LONG).show() else onOpenRoute(r.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("Iniciar ruta") }
                                    RouteStatus.IN_PROGRESS -> Button(onClick = { onOpenRoute(r.id) }, shape = RoundedCornerShape(12.dp)) { Text("Continuar") }
                                    RouteStatus.INTERRUPTED -> OutlinedButton(onClick = { vm.resume(r.id) }, shape = RoundedCornerShape(12.dp)) { Text("Reanudar") }
                                    else -> Unit
                                }
                                if (r.isEditable) TextButton(onClick = { onEdit(r.id) }) { Text("Editar") }
                                TextButton(onClick = { onOpenRoute(r.id) }) { Text("Ver detalle") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestChips(q: RideRequest) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TagChip(q.shift.label, RColors.BlueSoft, RColors.DarkBlue)
            TagChip(q.direction.label, RColors.Yellow, RColors.Text)
            if (q.validated) StatusChip("Validado")
        }
        Text("${q.shift.label} · ${q.direction.label}: ${q.direction.hint}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DriverRequestsScreen(onTab: (String) -> Unit, onDisabled: () -> Unit, vm: DriverViewModel = hiltViewModel()) {
    val requests by vm.requests.collectAsStateWithLifecycle()
    val routes by vm.myRoutes.collectAsStateWithLifecycle()
    val s by vm.session.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf<RideRequest?>(null) }
    val seats = routes.firstOrNull { it.status == RouteStatus.ACTIVE }?.seats ?: 0
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = { DriverBar(Routes.DRIVER_REQUESTS, onTab) }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Solicitudes", style = MaterialTheme.typography.headlineSmall)
            Text("Tienes ${seatsText(seats)} disponibles", color = RColors.Muted)
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(requests, key = { it.id }) { q ->
                    InfoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(q.passengerName)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(q.passengerName, style = MaterialTheme.typography.titleMedium)
                                Text("⭐ ${q.rating}", color = RColors.Muted)
                            }
                            if (q.status != RequestStatus.PENDING) StatusChip(q.status.label)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Punto: ${q.pickup} · ${seatsText(q.seats)}", color = RColors.Muted)
                        Spacer(Modifier.height(4.dp))
                        RequestChips(q)
                        Row {
                            TextButton(onClick = { info = q }) { Text("Ver quién es") }
                        }
                        if (q.status == RequestStatus.PENDING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { vm.reject(q.id) }, enabled = !s.isDisabled, modifier = Modifier.weight(1f)) {
                                    Text("Rechazar", color = RColors.Coral)
                                }
                                Button(
                                    onClick = { scope.launch { vm.accept(q.id)?.let { snack.showSnackbar(it) } } },
                                    enabled = !s.isDisabled, modifier = Modifier.weight(1f)
                                ) { Text("Aceptar") }
                            }
                        }
                    }
                }
            }
        }
    }
    info?.let { PassengerInfoDialog(it) { info = null } }
}

@Composable
fun DriverRouteDetailScreen(
    onBack: () -> Unit, onEdit: (Long) -> Unit, onReport: (Long) -> Unit, onDisabled: () -> Unit,
    vm: DriverViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val route by vm.route.collectAsStateWithLifecycle()
    val requests by vm.routeRequests.collectAsStateWithLifecycle()
    val s by vm.session.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var code by rememberSaveable { mutableStateOf("") }
    var codeResult by remember { mutableStateOf<Result<String>?>(null) }
    var showCancel by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<RideRequest?>(null) }
    var rateTarget by remember { mutableStateOf<RideRequest?>(null) }
    val reviewed by vm.reviewedKeys.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { RutitasTopBar("Detalle de ruta", onBack) },
        snackbarHost = { SnackbarHost(snack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        val r: Route? = route
        if (r == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        val stages = r.tripStages()
        val p = r.progress.coerceIn(0, stages.size)
        val accepted = requests.filter { it.status == RequestStatus.ACCEPTED && it.routeId == r.id }
        val pending = requests.filter { it.status == RequestStatus.PENDING }
        val live = r.status == RouteStatus.ACTIVE || r.status == RouteStatus.IN_PROGRESS

        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.title, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                StatusChip(r.status.label)
            }
            InfoCard {
                InfoLine("Horario", "${r.departure} → ${r.arrival}")
                InfoLine("Precio", "\$${r.price} por persona")
                InfoLine("Lugares disponibles", "${r.seats}")
                InfoLine("Tiempo de espera", "${r.waitMinutes} min por punto")
                InfoLine("Días", if (r.recurring) "${r.days} (recurrente)" else r.days)
                InfoLine("Vehículo", r.car)
            }
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)

            MeetingPointsSection(route = r, selected = null)

            // ---- Acciones según el estado ----
            when (r.status) {
                RouteStatus.ACTIVE -> {
                    PrimaryButton("Iniciar ruta", enabled = !s.isDisabled, onClick = {
                        vm.startRoute(r.id) { err -> if (err != null) Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                    })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onEdit(r.id) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Edit, null); Spacer(Modifier.width(6.dp)); Text("Editar")
                        }
                        OutlinedButton(
                            onClick = { showCancel = true }, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, RColors.Coral)
                        ) { Icon(Icons.Filled.Close, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp)); Text("Cancelar viaje", color = RColors.Coral) }
                    }
                }
                RouteStatus.IN_PROGRESS -> {
                    InfoCard {
                        Text("Ruta en curso", style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { p / stages.size.toFloat() }, Modifier.fillMaxWidth().padding(vertical = 8.dp), color = RColors.Blue)
                        stages.forEachIndexed { i, stage ->
                            val state = when { i < p -> 0; i == p -> 1; else -> 2 }
                            val label = when (state) { 0 -> "Completado"; 1 -> "En camino"; else -> "Pendiente" }
                            TimelineRow(stage, label, state, i == stages.lastIndex)
                        }
                    }
                    if (p < stages.size) {
                        val waitLeft = waitRemainingSeconds(r.waitStartedAt, r.waitMinutes)
                        val here = stages.getOrNull(p) ?: ""
                        if (waitLeft == null) {
                            PrimaryButton("Llegué a: $here", onClick = { vm.startWait() })
                            OutlinedButton(onClick = { vm.advance(stages.size) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
                                Text("Pasar al siguiente punto sin esperar")
                            }
                        } else {
                            InfoCard(color = RColors.BlueSoft) {
                                Text("Esperando pasajeros en $here", style = MaterialTheme.typography.titleMedium, color = RColors.DarkBlue)
                                Text(
                                    if (waitLeft > 0) formatMmSs(waitLeft) else "Tiempo terminado",
                                    style = MaterialTheme.typography.headlineMedium, color = RColors.Text
                                )
                                Text("Tiempo de espera: ${r.waitMinutes} min por punto", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                            }
                            PrimaryButton(if (waitLeft > 0) "Continuar (salir antes)" else "Continuar al siguiente punto", onClick = { vm.advance(stages.size) })
                            TextButton(onClick = { vm.endWait() }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancelar espera") }
                        }
                        TextButton(onClick = { onReport(r.id) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Icon(Icons.Filled.ReportProblem, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp))
                            Text("Reportar problema", color = RColors.Coral)
                        }
                    } else {
                        InfoCard(color = RColors.BlueSoft) { Text("¡Llegaste a UTTT! Puedes finalizar la ruta.") }
                        PrimaryButton("Finalizar ruta", onClick = { vm.finish() })
                    }
                }
                RouteStatus.INTERRUPTED -> {
                    InfoCard(color = RColors.CoralSoft) { Text("Esta ruta está interrumpida y no acepta nuevas reservas.") }
                    PrimaryButton("Reanudar ruta", onClick = { vm.resume(r.id) })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onEdit(r.id) }, modifier = Modifier.weight(1f)) { Text("Editar") }
                        OutlinedButton(onClick = { showCancel = true }, modifier = Modifier.weight(1f)) { Text("Cancelar viaje", color = RColors.Coral) }
                    }
                }
                RouteStatus.CANCELLED -> InfoCard(color = RColors.CoralSoft) { Text("Este viaje fue cancelado. Los pasajeros fueron notificados.") }
                RouteStatus.FINISHED -> InfoCard(color = RColors.BlueSoft) { Text("Ruta finalizada. ¡Gracias por compartir tu trayecto!") }
            }

            // ---- Validar código del pasajero ----
            if (live) {
                InfoCard {
                    Text("Validar código del pasajero", style = MaterialTheme.typography.titleMedium)
                    Text("Pídele su código de reserva antes de que aborde.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = code, onValueChange = { code = it.uppercase(); codeResult = null },
                        label = { Text("Código (ej. RT-3101)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("Validar código", enabled = !s.isDisabled && code.isNotBlank(), onClick = {
                        vm.validate(code) { res -> codeResult = res; if (res.isSuccess) code = "" }
                    })
                    codeResult?.let { res ->
                        Spacer(Modifier.height(8.dp))
                        Surface(color = if (res.isSuccess) RColors.BlueSoft else RColors.CoralSoft, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                res.getOrNull() ?: (res.exceptionOrNull()?.message ?: "Código no válido."),
                                Modifier.fillMaxWidth().padding(12.dp),
                                color = if (res.isSuccess) RColors.DarkBlue else Color(0xFFB42323)
                            )
                        }
                    }
                }
            }

            // ---- Pasajeros aceptados ----
            Text("Pasajeros (${accepted.size})", style = MaterialTheme.typography.titleMedium)
            if (accepted.isEmpty()) Text("Aún no has aceptado pasajeros en esta ruta.", color = RColors.Muted)
            accepted.forEach { q ->
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(q.passengerName)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(q.passengerName, style = MaterialTheme.typography.titleMedium)
                            Text("⭐ ${q.rating} · Punto: ${q.pickup}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { info = q }) { Text("Ver quién es") }
                    }
                    Spacer(Modifier.height(4.dp))
                    RequestChips(q)
                    if (r.status == RouteStatus.FINISHED) {
                        Spacer(Modifier.height(6.dp))
                        if ("route:${r.id}:req:${q.id}" in reviewed) StatusChip("Calificado")
                        else OutlinedButton(onClick = { rateTarget = q }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Star, null, tint = RColors.Yellow); Spacer(Modifier.width(6.dp)); Text("Calificar pasajero")
                        }
                    }
                }
            }

            // ---- Solicitudes pendientes ----
            if (live) {
                Text("Solicitudes por aceptar (${pending.size})", style = MaterialTheme.typography.titleMedium)
                if (pending.isEmpty()) Text("No hay solicitudes pendientes.", color = RColors.Muted)
                pending.forEach { q ->
                    InfoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(q.passengerName)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(q.passengerName, style = MaterialTheme.typography.titleMedium)
                                Text("⭐ ${q.rating} · Punto: ${q.pickup}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { info = q }) { Text("Ver quién es") }
                        }
                        Spacer(Modifier.height(4.dp))
                        RequestChips(q)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { vm.reject(q.id) }, enabled = !s.isDisabled, modifier = Modifier.weight(1f)) {
                                Text("Rechazar", color = RColors.Coral)
                            }
                            Button(
                                onClick = { scope.launch { vm.accept(q.id, r.id)?.let { snack.showSnackbar(it) } } },
                                enabled = !s.isDisabled, modifier = Modifier.weight(1f)
                            ) { Text("Aceptar viaje") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = RColors.Coral) },
            title = { Text("¿Cancelar este viaje?") },
            text = { Text("La ruta pasará a «Cancelada» y se avisará a los pasajeros aceptados.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancel = false
                    vm.cancelRoute {
                        Toast.makeText(context, "Viaje cancelado. Pasajeros notificados.", Toast.LENGTH_LONG).show()
                        onBack()
                    }
                }) { Text("Cancelar viaje", color = RColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { showCancel = false }) { Text("Volver") } }
        )
    }
    info?.let { PassengerInfoDialog(it) { info = null } }
    rateTarget?.let { q ->
        RateDialog(
            title = "Calificar a ${q.passengerName}", subtitle = "¿Cómo fue el viaje con este pasajero?",
            tagOptions = listOf("Puntual", "Amable", "Respetuoso", "Buena comunicación"),
            onDismiss = { rateTarget = null },
            onSubmit = { stars, tags, comment ->
                vm.rateRequest(q, stars, tags, comment)
                rateTarget = null
                Toast.makeText(context, "Calificación enviada", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DriverEarningsScreen(onBack: () -> Unit, vm: DriverViewModel = hiltViewModel()) {
    val e by vm.earnings.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Ganancias", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = RColors.Blue, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Este mes", color = Color.White.copy(alpha = 0.8f))
                    Text(money(e.month), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(Modifier.weight(1f)) { Text("Viajes", color = RColors.Muted); Text("${e.trips}", style = MaterialTheme.typography.titleLarge) }
                InfoCard(Modifier.weight(1f)) { Text("Promedio", color = RColors.Muted); Text(money(e.average), style = MaterialTheme.typography.titleLarge) }
            }
            Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium)
            InfoCard {
                e.movements.forEachIndexed { i, m ->
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(m.concept); Text(m.date, color = RColors.Muted, style = MaterialTheme.typography.bodySmall) }
                        Text("+${money(m.amount)}", color = RColors.Blue, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Publica una ruta nueva o, si se abre con una ruta existente, permite editarla. */
@Composable
fun PublishRouteScreen(onBack: () -> Unit, onDone: () -> Unit, onDisabled: () -> Unit, vm: DriverViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val s by vm.session.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val existing by vm.route.collectAsStateWithLifecycle()
    val editing = vm.editing

    var origin by rememberSaveable { mutableStateOf("Coyote") }
    var destination by rememberSaveable { mutableStateOf("UTTT") }
    var time by rememberSaveable { mutableStateOf("6:20 AM") }
    var seats by rememberSaveable { mutableIntStateOf(2) }
    var price by rememberSaveable { mutableStateOf("20") }
    var days by rememberSaveable { mutableStateOf(setOf("Lun", "Mar", "Mié", "Jue", "Vie")) }
    var recurring by rememberSaveable { mutableStateOf(true) }
    var wait by rememberSaveable { mutableIntStateOf(5) }
    var extraPoints by rememberSaveable { mutableStateOf(setOf<String>()) }
    var loaded by rememberSaveable { mutableStateOf(!editing) }
    var tried by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val allDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val priceNum = price.toIntOrNull()

    LaunchedEffect(existing) {
        val r = existing
        if (!loaded && r != null) {
            origin = r.origin; destination = r.destination; time = r.departure; seats = r.seats
            price = r.price.toString(); days = r.days.split(",").map { it.trim() }.toSet()
            recurring = r.recurring; wait = r.waitMinutes
            extraPoints = r.stops.drop(1).dropLast(1).toSet(); loaded = true
        }
    }
    val locked = editing && existing?.isEditable == false

    val pointOptions = listOf("Plaza Jardines", "Tula Centro", "Atitalaquia", "Tlahuelilpan Centro", "El Carmen Centro", "Coyote Centro")
        .filter { !it.contains(origin, true) && !it.contains(destination, true) }
    val originStop = if (origin.endsWith("Centro")) origin else "$origin Centro"
    val computedStops: List<String> = listOf(originStop) + pointOptions.filter { it in extraPoints } + destination
    val previewStops = computedStops
    val preview = Route(
        id = -1L, origin = origin, destination = destination, departure = time, arrival = time, price = priceNum ?: 0,
        seats = seats, driverName = "", driverRating = 0.0, car = "", stops = previewStops, status = RouteStatus.ACTIVE,
        ownedByMe = true, alternative = false, tag = null, days = "", recurring = true
    )

    Scaffold(
        topBar = { RutitasTopBar(if (editing) "Editar ruta" else "Publicar ruta", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (editing && !loaded) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(if (editing) "Actualiza tu trayecto" else "Programa tu trayecto", style = MaterialTheme.typography.titleLarge)
            if (locked) InfoCard(color = RColors.CoralSoft) { Text("Esta ruta ya inició o terminó, por eso no se puede editar.") }
            Text("Origen", style = MaterialTheme.typography.titleMedium)
            ChipGroup(DemoData.zones, setOf(origin)) { origin = it }
            Text("Destino", style = MaterialTheme.typography.titleMedium)
            ChipGroup(listOf("UTTT") + DemoData.zones, setOf(destination)) { destination = it }
            Text("Puntos de encuentro", style = MaterialTheme.typography.titleMedium)
            Text("El primero es tu origen. Agrega los puntos intermedios donde recogerás pasajeros.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            ChipGroup(pointOptions, extraPoints) { p -> extraPoints = if (p in extraPoints) extraPoints - p else extraPoints + p }
            if (origin != destination) MeetingPointsSection(route = preview, selected = null)
            TimeField(time, { time = it }, "Hora de salida", error = if (tried && !TimeUtils.isValid(time)) "Elige una hora" else null)
            Text("Tiempo de espera", style = MaterialTheme.typography.titleMedium)
            Text("Cuánto esperará el conductor a cada pasajero en el punto de encuentro.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            ChipGroup(listOf("5 min", "10 min", "15 min", "20 min"), setOf("$wait min")) { wait = it.substringBefore(" ").toInt() }
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lugares", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (seats > 1) seats-- }) { Icon(Icons.Filled.Remove, "Menos") }
                    Text("$seats", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { if (seats < 6) seats++ }) { Icon(Icons.Filled.Add, "Más") }
                }
            }
            RutitasField(price, { price = it.filter(Char::isDigit).take(3) }, "Precio por persona (MXN)", keyboardType = KeyboardType.Number,
                error = if (tried && (priceNum == null || priceNum <= 0)) "Escribe un precio" else null)
            Text("Días", style = MaterialTheme.typography.titleMedium)
            ChipGroup(allDays, days) { d -> days = if (d in days) days - d else days + d }
            if (tried && days.isEmpty()) Text("Elige al menos un día", color = MaterialTheme.colorScheme.error)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ruta recurrente", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Switch(checked = recurring, onCheckedChange = { recurring = it })
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)
            PrimaryButton(if (editing) "Guardar cambios" else "Publicar", loading = loading, enabled = !s.isDisabled && !locked, onClick = {
                tried = true; error = null
                when {
                    origin == destination -> error = "El origen y el destino no pueden ser iguales."
                    !TimeUtils.isValid(time) || priceNum == null || priceNum <= 0 || days.isEmpty() -> Unit
                    else -> vm.save(
                        origin, destination, time, seats, priceNum,
                        allDays.filter { it in days }.joinToString(", "), recurring, wait, computedStops,
                        onDone = {
                            Toast.makeText(context, if (editing) "Ruta actualizada" else "Ruta publicada", Toast.LENGTH_SHORT).show()
                            onDone()
                        },
                        onError = { error = it }
                    )
                }
            })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DriverProfileScreen(
    onTab: (String) -> Unit, onPassengerMode: () -> Unit, onLoggedOut: () -> Unit,
    onEditProfile: () -> Unit, onReviews: () -> Unit,
    vm: DriverViewModel = hiltViewModel(), profileVm: mx.edu.uttt.rutitas.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val s by vm.session.collectAsStateWithLifecycle()
    val incidents by vm.incidents.collectAsStateWithLifecycle()
    val stats by vm.driverStats.collectAsStateWithLifecycle()
    val actions = rememberPhotoActions { profileVm.setPhoto(it) }
    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DriverBar(Routes.DRIVER_PROFILE, onTab) }) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Perfil de conductor", style = MaterialTheme.typography.headlineSmall)
            InfoCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfilePhotoHeader(s.user?.name ?: "C", s.profilePhoto, 96, actions.take, actions.pick, profileVm::removePhoto)
                    Spacer(Modifier.height(8.dp))
                    Text(s.user?.name ?: "Conductor", style = MaterialTheme.typography.titleLarge)
                    Text(s.user?.university ?: "", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatBlock(if (stats.second == 0) "–" else "⭐ ${stats.first}", "Calificación")
                        StatBlock("${stats.second}", "Reseñas")
                        StatBlock(if (s.driverStatus == mx.edu.uttt.rutitas.model.DriverStatus.APPROVED) "Sí" else "No", "Verificado")
                    }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("Editar perfil", onEditProfile)
                }
            }
            InfoCard {
                Text("Mi horario", style = MaterialTheme.typography.titleMedium)
                InfoLine("Turno", s.shift.label)
                InfoLine("Entrada", s.entryTime)
                InfoLine("Salida", s.exitTime)
                TextButton(onClick = onEditProfile) { Icon(Icons.Filled.Schedule, null); Spacer(Modifier.width(6.dp)); Text("Editar horario") }
            }
            InfoCard {
                Text("Vehículo", style = MaterialTheme.typography.titleMedium)
                Text(s.vehicle.description.ifBlank { "Sin datos" })
                if (s.vehicle.plates.isNotBlank()) Text("Placas ${s.vehicle.plates} · ${seatsText(s.vehicle.seats)}", color = RColors.Muted)
            }
            InfoCard {
                Text("Incidencias recientes", style = MaterialTheme.typography.titleMedium)
                if (incidents.isEmpty()) Text("Sin incidencias", color = RColors.Muted)
                incidents.take(4).forEach {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(Modifier.weight(1f)) { Text(it.type.label); Text(it.routeTitle, color = RColors.Muted, style = MaterialTheme.typography.bodySmall) }
                        Text(it.whenLabel, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            SecondaryButton("Reseñas y calificaciones", onReviews)
            SecondaryButton("Cambiar a modo pasajero", onPassengerMode)
            TextButton(onClick = { profileVm.logout(); onLoggedOut() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
            profileVm.deleteAccount {
                Toast.makeText(context, "Tu cuenta fue eliminada", Toast.LENGTH_LONG).show()
                onLoggedOut()
            }
        })
    }
}
