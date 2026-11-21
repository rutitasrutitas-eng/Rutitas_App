package mx.edu.uttt.rutitas.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.edu.uttt.rutitas.network.BackendUserDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesion independiente para usuarios del Backend.
 * No convierte usuarios reales en entidades locales de Room.
 */
@Singleton
class BackendSessionState @Inject constructor() {

    private val _user = MutableStateFlow<BackendUserDto?>(null)

    val user: StateFlow<BackendUserDto?> = _user.asStateFlow()

    fun setUser(value: BackendUserDto) {
        _user.value = value
    }

    fun clear() {
        _user.value = null
    }
}
