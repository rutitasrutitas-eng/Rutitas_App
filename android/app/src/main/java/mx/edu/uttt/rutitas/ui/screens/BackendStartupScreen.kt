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
import mx.edu.uttt.rutitas.viewmodel.BackendStartupViewModel

@Composable
fun BackendStartupScreen(
    onFinished: (Boolean) -> Unit,
    vm: BackendStartupViewModel = hiltViewModel()
) {
    val restored = vm.restored.collectAsStateWithLifecycle().value

    LaunchedEffect(restored) {
        if (restored != null) {
            onFinished(restored)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Comprobando sesion...")
        }
    }
}