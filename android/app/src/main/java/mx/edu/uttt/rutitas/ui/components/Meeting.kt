package mx.edu.uttt.rutitas.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.Route
import mx.edu.uttt.rutitas.ui.theme.RColors

@SuppressLint("MissingPermission")
private fun lastKnownZone(context: Context): String? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val loc = lm.getProviders(true)
        .mapNotNull { p -> try { lm.getLastKnownLocation(p) } catch (e: SecurityException) { null } }
        .maxByOrNull { it.time } ?: return null
    return GeoPlaces.nearestZone(loc.latitude, loc.longitude)
}

/** Diálogo para agregar la ubicación: GPS real (si hay permiso) o elegir la zona manualmente. */
@Composable
fun LocationPickerDialog(onDismiss: () -> Unit, onZone: (String) -> Unit) {
    val context = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            val z = lastKnownZone(context)
            if (z != null) onZone(z)
            else Toast.makeText(context, "No pudimos obtener tu GPS. Elige tu zona.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Sin permiso de ubicación. Elige tu zona manualmente.", Toast.LENGTH_LONG).show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar mi ubicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                    modifier = Modifier.fillMaxWidth()
                ) { Icon(Icons.Filled.MyLocation, null); Spacer(Modifier.width(8.dp)); Text("Usar mi GPS") }
                Text("o elige tu zona:", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                ChipGroup(DemoData.zones, emptySet()) { onZone(it) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Mapa con los puntos de encuentro numerados. Al agregar la ubicación se dibuja "Tú"
 * y la línea al punto de encuentro más cercano.
 */
@Composable
fun MeetingPointsSection(
    route: Route, selected: String?, modifier: Modifier = Modifier,
    onPointClick: ((String) -> Unit)? = null
) {
    var userZone by rememberSaveable { mutableStateOf<String?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    val points = route.stops.dropLast(1)
    val nearest = userZone?.let { MapPlaces.nearestPoint(points, it) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RutitasMap(
            routes = listOf(route), modifier = Modifier.fillMaxWidth().height(220.dp), highlight = route.id,
            meetingPoints = points, selectedPoint = selected, userZone = userZone
        )
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.LocationOn, null)
            Spacer(Modifier.width(8.dp))
            Text(if (userZone == null) "Agregar mi ubicación" else "Cambiar mi ubicación ($userZone)")
        }
        if (nearest != null) {
            InfoCard(color = RColors.BlueSoft) {
                Text("Punto de encuentro más cercano a ti", style = MaterialTheme.typography.labelMedium, color = RColors.Muted)
                Text(nearest, style = MaterialTheme.typography.titleMedium, color = RColors.DarkBlue)
                if (onPointClick != null && nearest != selected) {
                    TextButton(onClick = { onPointClick(nearest) }) { Text("Elegir este punto") }
                }
            }
        }
        Text("Puntos de encuentro", style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            points.forEachIndexed { i, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagChip("${i + 1}", if (name == selected) RColors.Coral else RColors.Yellow)
                    Spacer(Modifier.width(10.dp))
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    if (onPointClick != null) {
                        TextButton(onClick = { onPointClick(name) }, enabled = name != selected) {
                            Text(if (name == selected) "Elegido" else "Elegir")
                        }
                    }
                }
            }
        }
    }
    if (showPicker) {
        LocationPickerDialog(onDismiss = { showPicker = false }, onZone = { userZone = it; showPicker = false })
    }
}
