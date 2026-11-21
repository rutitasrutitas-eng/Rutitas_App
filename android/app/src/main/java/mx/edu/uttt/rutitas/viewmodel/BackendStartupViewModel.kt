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

@HiltViewModel
class BackendStartupViewModel @Inject constructor(
    private val auth: BackendAuthRepository
) : ViewModel() {

    private val _restored = MutableStateFlow<Boolean?>(null)
    val restored = _restored.asStateFlow()

    init {
        viewModelScope.launch {
            _restored.value = try {
                auth.restoreSession()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
        }
    }
}