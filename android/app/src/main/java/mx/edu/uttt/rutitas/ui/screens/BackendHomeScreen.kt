package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.viewmodel.BackendHomeViewModel

@Composable
fun BackendHomeScreen(
    onOpenRoutes: () -> Unit,
    onOpenProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: BackendHomeViewModel = hiltViewModel()
) {
    val user by vm.user.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Text(
                    "Rutitas - Cuenta",
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Cuenta conectada al Backend",
                style = MaterialTheme.typography.headlineSmall
            )

            if (user != null) {
                Text("Nombre: ${user?.displayName.orEmpty()}")
                Text("Correo: ${user?.email.orEmpty()}")

                OutlinedButton(
                    onClick = onOpenProfile,
                    enabled = !ui.loading
                ) {
                    Text("Consultar y editar perfil")
                }

                OutlinedButton(
                    onClick = onOpenRoutes,
                    enabled = !ui.loading
                ) {
                    Text("Consultar rutas Backend")
                }
            } else {
                Text("No hay una sesion activa.")
            }

            Text(
                "Las reservas y funciones de conductor aun no estan disponibles en este modo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ui.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (user == null || ui.error != null) {
                        onLoggedOut()
                    } else {
                        vm.logout(onLoggedOut)
                    }
                },
                enabled = !ui.loading
            ) {
                Text(
                    when {
                        ui.loading -> "Cerrando sesion..."
                        user == null || ui.error != null ->
                            "Volver al inicio de sesion"
                        else -> "Cerrar sesion"
                    }
                )
            }
        }
    }
}
