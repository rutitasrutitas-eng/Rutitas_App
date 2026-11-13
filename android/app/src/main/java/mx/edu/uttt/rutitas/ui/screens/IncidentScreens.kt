package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.BorderStroke
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
import mx.edu.uttt.rutitas.model.IncidentType
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.IncidentViewModel

@Composable
fun IncidentScreen(onBack: () -> Unit, onReported: (String) -> Unit, vm: IncidentViewModel = hiltViewModel()) {
    val route by vm.route.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var confirm by remember { mutableStateOf(false) }

    fun submit(type: IncidentType) = vm.report(type) { onReported(type.name) }

    Scaffold(topBar = { RutitasTopBar("Reportar problema", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Selecciona el problema", style = MaterialTheme.typography.headlineSmall)
            route?.let { Text("Ruta en curso: ${it.title}", color = RColors.Muted) }
            IncidentType.entries.forEach { t ->
                val sel = selected == t.name
                Card(
                    onClick = { selected = t.name }, shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (sel) RColors.BlueSoft else Color.White),
                    border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) RColors.Blue else RColors.Line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sel, onClick = { selected = t.name })
                        Text(t.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            PrimaryButton("Continuar", enabled = selected != null, onClick = {
                val type = IncidentType.valueOf(selected!!)
                if (type == IncidentType.BREAKDOWN) confirm = true else submit(type)
            })
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = RColors.Coral) },
            title = { Text("¿Confirmar falla del vehículo?") },
            text = { Text("La ruta pasará a «Interrumpida», se bloquearán nuevas reservas y se avisará a los pasajeros.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; submit(IncidentType.BREAKDOWN) }) { Text("Reportar incidencia", color = RColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar y continuar viaje") } }
        )
    }
}

@Composable
fun IncidentResultScreen(
    typeName: String, onHome: () -> Unit, onRoute: (Long) -> Unit,
    vm: IncidentViewModel = hiltViewModel()
) {
    val type = runCatching { IncidentType.valueOf(typeName) }.getOrDefault(IncidentType.OTHER)
    val latest by vm.latest.collectAsStateWithLifecycle()
    val alternatives by vm.alternatives.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Incidencia") }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = RColors.BlueSoft, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CheckCircle, null, tint = RColors.Blue, modifier = Modifier.size(46.dp)) }
            }
            Text("Incidencia registrada", style = MaterialTheme.typography.headlineSmall)
            InfoCard {
                Text(type.label, style = MaterialTheme.typography.titleMedium)
                if (type == IncidentType.BREAKDOWN) {
                    Spacer(Modifier.height(8.dp))
                    StatusChip("Interrumpida")
                    Spacer(Modifier.height(8.dp))
                    val n = latest?.notified ?: 0
                    Text("• $n pasajero(s) notificados en la simulación")
                    Text("• Nuevas reservas bloqueadas en esta ruta")
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text("Avisamos a la comunidad de tu ruta. El viaje puede continuar.", color = RColors.Muted)
                }
            }
            if (type == IncidentType.BREAKDOWN) {
                Text("Rutas alternativas", Modifier.align(Alignment.Start), style = MaterialTheme.typography.titleMedium)
                if (alternatives.isEmpty()) Text("Por ahora no hay alternativas disponibles.", color = RColors.Muted)
                alternatives.forEach { r -> RouteCard(r, onClick = { onRoute(r.id) }) }
            }
            PrimaryButton("Volver al inicio", onHome)
            Spacer(Modifier.height(12.dp))
        }
    }
}
