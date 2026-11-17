package mx.edu.uttt.rutitas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.data.BackendSessionState
import mx.edu.uttt.rutitas.repository.BackendProfileRepository
import mx.edu.uttt.rutitas.repository.PhoneAction
import javax.inject.Inject

data class BackendProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val displayName: String = "",
    val phone: String = "",
    val phoneAction: PhoneAction = PhoneAction.KEEP,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class BackendProfileViewModel @Inject constructor(
    private val repository: BackendProfileRepository,
    session: BackendSessionState
) : ViewModel() {
    val user = session.user

    private val _ui = MutableStateFlow(BackendProfileUiState())
    val ui = _ui.asStateFlow()

    init {
        refresh()
    }

    private fun errorMessage(e: Exception): String =
        mx.edu.uttt.rutitas.network.BackendErrorHandler
            .from(e)
            .message
    fun refresh() {
        if (_ui.value.saving) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val profile = repository.getProfile()
                _ui.value = BackendProfileUiState(
                    displayName = profile.displayName,
                    phone = profile.phone.orEmpty()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = errorMessage(e)
                )
            }
        }
    }

    fun changeName(value: String) {
        _ui.value = _ui.value.copy(
            displayName = value,
            message = null
        )
    }

    fun changePhone(value: String) {
        _ui.value = _ui.value.copy(
            phone = value,
            phoneAction = PhoneAction.UPDATE,
            message = null
        )
    }

    fun keepPhone() {
        _ui.value = _ui.value.copy(
            phoneAction = PhoneAction.KEEP,
            message = null
        )
    }

    fun removePhone() {
        _ui.value = _ui.value.copy(
            phoneAction = PhoneAction.REMOVE,
            message = null
        )
    }

    fun save() {
        val state = _ui.value
        if (state.loading || state.saving || user.value == null) return

        val name = state.displayName.trim()
        if (name.isEmpty() || name.length > 80) {
            _ui.value = state.copy(
                error = "El nombre debe tener entre 1 y 80 caracteres."
            )
            return
        }

        if (state.phoneAction == PhoneAction.UPDATE &&
            !Regex("^\\+[1-9][0-9]{7,14}$").matches(state.phone.trim())
        ) {
            _ui.value = state.copy(
                error = "Usa un telefono internacional, por ejemplo +525512345678."
            )
            return
        }

        viewModelScope.launch {
            _ui.value = state.copy(saving = true, error = null, message = null)
            try {
                val updated = repository.updateProfile(
                    displayName = name,
                    phoneAction = state.phoneAction,
                    phone = state.phone
                )

                _ui.value = BackendProfileUiState(
                    displayName = updated.displayName,
                    phone = updated.phone.orEmpty(),
                    message = "Perfil actualizado correctamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    saving = false,
                    error = errorMessage(e)
                )
            }
        }
    }
}
