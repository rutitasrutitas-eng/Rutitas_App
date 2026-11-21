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
import mx.edu.uttt.rutitas.repository.PhoneAction
import mx.edu.uttt.rutitas.viewmodel.BackendProfileViewModel

@Composable
fun BackendProfileScreen(
    onBack: () -> Unit,
    vm: BackendProfileViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Text(
                    "Mi perfil - Backend",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Volver a mi cuenta")
            }

            if (ui.loading) {
                CircularProgressIndicator()
                Text("Consultando perfil del servidor...")
            } else if (user == null) {
                Text("No hay una sesion activa.")
            } else {
                Text("Correo: ${user?.email.orEmpty()}")

                OutlinedTextField(
                    value = ui.displayName,
                    onValueChange = vm::changeName,
                    label = { Text("Nombre") },
                    singleLine = true,
                    enabled = !ui.saving,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ui.phone,
                    onValueChange = vm::changePhone,
                    label = { Text("Telefono internacional") },
                    singleLine = true,
                    enabled = !ui.saving &&
                        ui.phoneAction != PhoneAction.REMOVE,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Que deseas hacer con el telefono?")

                Text(
                    when (ui.phoneAction) {
                        PhoneAction.KEEP -> "Conservar telefono actual"
                        PhoneAction.UPDATE -> "Actualizar telefono"
                        PhoneAction.REMOVE -> "Eliminar telefono"
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = vm::keepPhone,
                        enabled = !ui.saving
                    ) {
                        Text("Conservar")
                    }

                    OutlinedButton(
                        onClick = vm::removePhone,
                        enabled = !ui.saving
                    ) {
                        Text("Eliminar")
                    }
                }

                Button(
                    onClick = vm::save,
                    enabled = !ui.saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (ui.saving) "Guardando..." else "Guardar cambios")
                }
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            ui.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            if (ui.error != null && user != null) {
                TextButton(onClick = vm::refresh) {
                    Text("Reintentar consulta")
                }
            }
        }
    }
}
