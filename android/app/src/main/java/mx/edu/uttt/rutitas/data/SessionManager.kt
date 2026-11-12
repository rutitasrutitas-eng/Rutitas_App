package mx.edu.uttt.rutitas.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.edu.uttt.rutitas.model.Session
import javax.inject.Inject
import javax.inject.Singleton

/** Estado de sesión en memoria (usuario, horario, estado de conductor y de la cuenta). */
@Singleton
class SessionManager @Inject constructor() {
    private val _state = MutableStateFlow(Session())
    val state: StateFlow<Session> = _state.asStateFlow()
    val current: Session get() = _state.value
    fun update(block: (Session) -> Session) = _state.update(block)
}
