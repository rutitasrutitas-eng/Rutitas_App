package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.viewmodel.BackendRoutesViewModel

@Composable
fun BackendRoutesScreen(
    onBack: () -> Unit,
    vm: BackendRoutesViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Text(
                    "Rutitas - Rutas Backend",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Volver a mi cuenta")
            }

            val detail = ui.detail

            if (detail != null) {
                Text(
                    detail.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Codigo: ${detail.code}")
                Text(detail.description)
                Text("Distancia: ${detail.distanceKm} km")
                Text("Duracion estimada: ${detail.estimatedDurationMin} min")
                Text("Paradas registradas: ${detail.stops.size}")

                Button(onClick = vm::closeDetail) {
                    Text("Volver al listado")
                }
            } else {
                Text(
                    "Consultar rutas disponibles",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = ui.search,
                    onValueChange = vm::changeSearch,
                    label = { Text("Buscar rutas") },
                    singleLine = true,
                    enabled = !ui.loading && !ui.detailLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = vm::searchRoutes,
                    enabled = !ui.loading && !ui.detailLoading
                ) {
                    Text("Buscar")
                }

                if (ui.loading || ui.detailLoading) {
                    CircularProgressIndicator()
                }

                if (!ui.loading && !ui.detailLoading) {
                    Text("Rutas encontradas: ${ui.total}")

                    if (ui.routes.isEmpty() && ui.error == null) {
                        Text("No se encontraron rutas.")
                    }

                    ui.routes.forEach { route ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    route.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Codigo: ${route.code}")
                                Text("Distancia: ${route.distanceKm} km")
                                Text(
                                    "Duracion: ${route.estimatedDurationMin} min"
                                )

                                OutlinedButton(
                                    onClick = { vm.openRoute(route.id) }
                                ) {
                                    Text("Ver detalles")
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.loadRoutes(ui.page - 1) },
                            enabled = ui.page > 1
                        ) {
                            Text("Anterior")
                        }

                        OutlinedButton(
                            onClick = { vm.loadRoutes(ui.page + 1) },
                            enabled = ui.page.toLong() < ui.totalPages
                        ) {
                            Text("Siguiente")
                        }
                    }

                    Text("Pagina ${ui.page} de ${ui.totalPages}")

                    TextButton(
                        onClick = { vm.loadRoutes() }
                    ) {
                        Text("Actualizar rutas")
                    }
                }
            }

            ui.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
