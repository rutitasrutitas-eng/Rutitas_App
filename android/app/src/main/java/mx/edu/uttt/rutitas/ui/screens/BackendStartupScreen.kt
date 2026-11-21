package mx.edu.uttt.rutitas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.viewmodel.BackendStartupStatus
import mx.edu.uttt.rutitas.viewmodel.BackendStartupViewModel

@Composable
fun BackendStartupScreen(
    onFinished: (Boolean) -> Unit,
    vm: BackendStartupViewModel = hiltViewModel()
) {
    val status = vm.status.collectAsStateWithLifecycle().value

    LaunchedEffect(status) {
        when (status) {
            BackendStartupStatus.VERIFIED -> onFinished(true)
            BackendStartupStatus.LOGIN_REQUIRED -> onFinished(false)
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (status) {
                BackendStartupStatus.CHECKING -> {
                    CircularProgressIndicator()
                    Text("Comprobando sesion...")
                }

                BackendStartupStatus.RETRY -> {
                    Text(
                        "No se pudo verificar la sesion con el servidor.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Tu sesion sigue guardada en este dispositivo."
                    )
                    Button(onClick = vm::retry) {
                        Text("Reintentar conexion")
                    }
                    TextButton(onClick = { onFinished(false) }) {
                        Text("Ir al inicio de sesion")
                    }
                }

                BackendStartupStatus.VERIFIED,
                BackendStartupStatus.LOGIN_REQUIRED -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
