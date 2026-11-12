package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.model.Review
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.DriverPublicReviewsViewModel
import mx.edu.uttt.rutitas.viewmodel.ReviewsViewModel

/**
 * Historial de reseñas. Como pasajero: las calificaciones que te dieron los conductores y las que tú diste.
 * Como conductor: las que te dieron los pasajeros y las que tú diste a ellos.
 */
@Composable
fun ReviewsScreen(onBack: () -> Unit, vm: ReviewsViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val list = if (tab == 0) ui.received else ui.sent
    val who = if (ui.asDriver) "pasajeros" else "conductores"
    Scaffold(
        topBar = { RutitasTopBar(if (ui.asDriver) "Reseñas como conductor" else "Reseñas y calificaciones", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (ui.received.isEmpty()) "Sin calificaciones" else "⭐ ${ui.average}", style = MaterialTheme.typography.headlineSmall)
                        Text("${ui.received.size} reseñas recibidas de $who", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${ui.sent.size} enviadas", color = RColors.Muted)
                }
            }
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Recibidas (${ui.received.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Enviadas (${ui.sent.size})") })
            }
            if (list.isEmpty()) {
                Text(
                    if (tab == 0) "Aún no has recibido reseñas." else "Aún no has calificado a nadie.",
                    color = RColors.Muted
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { r -> ReviewCard(r, received = tab == 0) }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun ReviewCard(r: Review, received: Boolean) {
    val person = if (received) r.fromName else r.toName
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(person, 40)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (received) "De $person" else "Para $person", style = MaterialTheme.typography.titleMedium)
                Text("${r.routeTitle} · ${r.dateLabel}", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            StarRow(r.stars, null, size = 18)
        }
        if (r.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                r.tags.forEach { TagChip(it, RColors.BlueSoft, RColors.DarkBlue) }
            }
        }
        if (r.comment.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(r.comment)
        }
    }
}

/** Lo que ve el pasajero: reseñas que otros pasajeros dejaron a un conductor. */
@Composable
fun DriverPublicReviewsScreen(onBack: () -> Unit, vm: DriverPublicReviewsViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Reseñas del conductor", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(ui.name, 56)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(ui.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (ui.reviews.isEmpty()) "Aún sin reseñas" else "⭐ ${ui.average} · ${ui.reviews.size} reseñas",
                            color = RColors.Muted
                        )
                    }
                }
            }
            if (ui.reviews.isEmpty()) Text("Este conductor todavía no tiene reseñas de pasajeros.", color = RColors.Muted)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ui.reviews, key = { it.id }) { r -> ReviewCard(r, received = true) }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}
