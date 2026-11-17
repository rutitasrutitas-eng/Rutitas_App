package mx.edu.uttt.rutitas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.repository.BackendAuthRepository
import javax.inject.Inject

data class BackendHomeUiState(
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BackendHomeViewModel @Inject constructor(
    private val auth: BackendAuthRepository,
    private val backendSession: BackendSessionState
) : ViewModel() {

    val user = backendSession.user

    private val _ui = MutableStateFlow(BackendHomeUiState())
    val ui: StateFlow<BackendHomeUiState> = _ui.asStateFlow()

    fun logout(onDone: () -> Unit) {
        if (_ui.value.loading) return

        viewModelScope.launch {
            _ui.value = BackendHomeUiState(loading = true)

            try {
                auth.logout()
                _ui.value = BackendHomeUiState()
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.value = BackendHomeUiState(
                    error = "Se cerro la sesion local, pero no se pudo confirmar la revocacion del token en el servidor."
                )
            }
        }
    }
}
