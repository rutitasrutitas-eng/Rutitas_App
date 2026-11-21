package mx.edu.uttt.rutitas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.repository.BackendAuthRepository
import javax.inject.Inject

enum class BackendStartupStatus {
    CHECKING,
    VERIFIED,
    LOGIN_REQUIRED,
    RETRY
}

@HiltViewModel
class BackendStartupViewModel @Inject constructor(
    private val auth: BackendAuthRepository
) : ViewModel() {

    private val _status = MutableStateFlow(
        BackendStartupStatus.CHECKING
    )
    val status = _status.asStateFlow()

    init {
        checkSession()
    }

    fun retry() {
        if (_status.value == BackendStartupStatus.CHECKING) return
        checkSession()
    }

    private fun checkSession() {
        _status.value = BackendStartupStatus.CHECKING

        viewModelScope.launch {
            val restored = try {
                auth.restoreSession()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }

            _status.value = when {
                restored -> BackendStartupStatus.VERIFIED
                auth.hasValidSession() -> BackendStartupStatus.RETRY
                else -> BackendStartupStatus.LOGIN_REQUIRED
            }
        }
    }
}
