package mx.edu.uttt.rutitas.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.model.RouteStatus
import mx.edu.uttt.rutitas.model.Shift
import mx.edu.uttt.rutitas.model.TripDirection
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.utils.money
import mx.edu.uttt.rutitas.utils.seatsText
import mx.edu.uttt.rutitas.viewmodel.BookingViewModel
import mx.edu.uttt.rutitas.viewmodel.averageStars

@Composable
fun RouteDetailScreen(
    onBack: () -> Unit, onReserve: (Long) -> Unit, onLogin: () -> Unit, onDisabled: () -> Unit,
    onDriverReviews: (String) -> Unit,
    vm: BookingViewModel = hiltViewModel()
) {
    val route by vm.route.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val reviews by vm.driverReviews.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Detalle de ruta", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        val r = route
        if (r == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (r.tag != null) TagChip(r.tag)
            Text(r.title, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${r.departure} → ${r.arrival}", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("\$${r.price}", style = MaterialTheme.typography.headlineSmall, color = RColors.Blue)
            }
            Text(seatsText(r.seats) + " disponibles", color = RColors.Muted)
            if (r.status == RouteStatus.INTERRUPTED) {
                InfoCard(color = RColors.CoralSoft) { Text("Esta ruta está interrumpida y no acepta nuevas reservas.") }
            }
            InfoCard {
                Text("Conductor", color = RColors.Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(r.driverName)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.driverName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (reviews.isEmpty()) "⭐ ${r.driverRating}" else "⭐ ${averageStars(reviews)} · ${reviews.size} reseñas",
                            color = RColors.Muted
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Auto: ${r.car}", color = RColors.Text)
                if (reviews.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Lo que dicen los pasajeros", style = MaterialTheme.typography.labelMedium, color = RColors.Muted)
                    reviews.take(2).forEach { rv ->
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRow(rv.stars, null, size = 14)
                            Spacer(Modifier.width(6.dp))
                            Text(rv.fromName, style = MaterialTheme.typography.bodySmall, color = RColors.Muted)
                        }
                        if (rv.comment.isNotBlank()) Text(rv.comment, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                TextButton(onClick = { onDriverReviews(r.driverName) }) { Text("Ver todas las reseñas del conductor") }
            }
            InfoCard {
                Text("Recorrido", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                r.stops.forEachIndexed { i, s -> TimelineRow(s, null, if (i == 0) 0 else 2, i == r.stops.lastIndex) }
                Text("El conductor espera hasta ${r.waitMinutes} min en cada punto de encuentro.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            MeetingPointsSection(route = r, selected = null)
            if (session.isDisabled) DisabledBanner(onDetails = onDisabled)
            val label = when {
                r.status == RouteStatus.INTERRUPTED -> "Ruta interrumpida"
                r.seats == 0 -> "Sin lugares disponibles"
                else -> "Reservar"
            }
            PrimaryButton(
                label, enabled = r.isBookable && !session.isDisabled,
                onClick = { if (session.user == null) onLogin() else onReserve(r.id) }
            )
            if (session.user == null) Text("Inicia sesión para reservar.", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ReserveScreen(
    onBack: () -> Unit, onConfirmed: (Long) -> Unit, onDisabled: () -> Unit,
    vm: BookingViewModel = hiltViewModel()
) {
    val route by vm.route.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    var seats by rememberSaveable { mutableIntStateOf(1) }
    var pickup by rememberSaveable { mutableStateOf<String?>(null) }
    var shiftName by rememberSaveable { mutableStateOf(session.shift.name) }
    var directionName by rememberSaveable { mutableStateOf(TripDirection.ENTRADA.name) }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { RutitasTopBar("Reserva", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        val r = route
        if (r == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        val point = pickup ?: r.stops.first()
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoCard {
                SummaryRow("Ruta", r.title)
                SummaryRow("Hora", "${r.departure} → ${r.arrival}")
                SummaryRow("Conductor", r.driverName)
            }
            Text("Punto de encuentro", style = MaterialTheme.typography.titleMedium)
            MeetingPointsSection(route = r, selected = point, onPointClick = { pickup = it })
            Text("Tu horario", style = MaterialTheme.typography.titleMedium)
            ChipGroup(Shift.entries.map { it.label }, setOf(Shift.valueOf(shiftName).label)) { label ->
                shiftName = Shift.entries.first { it.label == label }.name
            }
            Text("Tipo de viaje", style = MaterialTheme.typography.titleMedium)
            ChipGroup(TripDirection.entries.map { it.label }, setOf(TripDirection.valueOf(directionName).label)) { label ->
                directionName = TripDirection.entries.first { it.label == label }.name
            }
            Text(TripDirection.valueOf(directionName).hint, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lugares", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (seats > 1) seats-- }) { Icon(Icons.Filled.Remove, "Menos") }
                    Text("$seats", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { if (seats < r.seats) seats++ }) { Icon(Icons.Filled.Add, "Más") }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row {
                    Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(money(r.price * seats), style = MaterialTheme.typography.titleLarge, color = RColors.Blue)
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (session.isDisabled) DisabledBanner(onDetails = onDisabled)
            PrimaryButton("Confirmar reserva", loading = loading, enabled = !session.isDisabled && r.isBookable, onClick = {
                error = null
                vm.reserve(point, seats, Shift.valueOf(shiftName), TripDirection.valueOf(directionName), onConfirmed) { error = it }
            })
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, Modifier.weight(1f), color = RColors.Muted)
        Text(value, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    }
}

@Composable
fun BookingConfirmedScreen(onViewTrip: (Long) -> Unit, onHome: () -> Unit, vm: BookingViewModel = hiltViewModel()) {
    val booking by vm.booking.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Reserva confirmada") }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        val b = booking
        if (b == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = CircleShape, color = RColors.BlueSoft, modifier = Modifier.size(88.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CheckCircle, null, tint = RColors.Blue, modifier = Modifier.size(52.dp)) }
            }
            Text("¡Tu lugar está reservado!", style = MaterialTheme.typography.headlineSmall)
            InfoCard {
                Text(b.title, style = MaterialTheme.typography.titleLarge)
                Text(b.departure, color = RColors.Muted)
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                SummaryRow("Punto", b.pickup)
                SummaryRow("Conductor", b.driverName)
                SummaryRow("Horario", "${b.shift.label} · ${b.direction.label}")
                SummaryRow("Total", money(b.total))
            }
            Surface(color = RColors.Yellow, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Código", style = MaterialTheme.typography.labelMedium)
                    Text(b.code, style = MaterialTheme.typography.headlineMedium, color = RColors.Text)
                }
            }
            PrimaryButton("Ver mi viaje", onClick = { onViewTrip(b.id) })
            TextButton(onClick = onHome) { Text("Volver al mapa") }
        }
    }
}
