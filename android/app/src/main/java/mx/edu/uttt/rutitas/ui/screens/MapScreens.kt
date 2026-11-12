package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.MapViewModel

@Composable
fun MapScreen(
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onRoute: (Long) -> Unit,
    onDisabled: () -> Unit,
    onTab: (String) -> Unit,
    vm: MapViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PassengerBar(Routes.MAP, onTab) }
    ) { pad ->
        if (state.loading && state.routes.isEmpty()) {
            LoadingBox(Modifier.padding(pad))
        } else {
            LazyColumn(
                Modifier.padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Rutas cerca de ti", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                state.userName?.let { "Hola, ${it.substringBefore(' ')}" } ?: "Explora sin iniciar sesión",
                                color = RColors.Muted
                            )
                        }
                        if (state.userName == null) {
                            TextButton(onClick = onLogin) { Text("Iniciar sesión") }
                        }
                    }
                }
                if (state.accountDisabled) item { DisabledBanner(onDetails = onDisabled) }
                item {
                    Surface(
                        onClick = onSearch, shape = RoundedCornerShape(14.dp), color = Color.White,
                        shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Search, null, tint = RColors.Muted)
                            Spacer(Modifier.width(10.dp))
                            Text("¿A dónde vas?", color = RColors.Muted)
                        }
                    }
                }
                item { RutitasMap(routes = state.routes, modifier = Modifier.fillMaxWidth().height(240.dp)) }
                item { Text("Recomendadas para ti", style = MaterialTheme.typography.titleMedium) }
                items(state.routes, key = { it.id }) { r -> RouteCard(r, onClick = { onRoute(r.id) }) }
                if (state.routes.isEmpty()) {
                    item { Text("Por ahora no hay rutas disponibles. Vuelve en unos minutos.", color = RColors.Muted) }
                }
                if (state.userName == null) {
                    item { PrimaryButton("Iniciar sesión", onLogin) }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(onRoute: (Long) -> Unit, onTab: (String) -> Unit, vm: MapViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var zone by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = remember(state.routes, query, zone) {
        state.routes.filter { r ->
            (query.isBlank() || r.title.contains(query, true) || r.stops.any { it.contains(query, true) }) &&
                (zone == null || r.origin == zone)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PassengerBar(Routes.SEARCH, onTab) }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Buscar", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("¿A dónde vas?") }, singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) }, shape = RoundedCornerShape(14.dp)
            )
            ChipGroup(DemoData.zones, setOfNotNull(zone)) { zone = if (zone == it) null else it }
            if (filtered.isEmpty()) {
                Text("No encontramos rutas con esos datos. Prueba otra zona.", color = RColors.Muted)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { r -> RouteCard(r, onClick = { onRoute(r.id) }) }
            }
        }
    }
}
