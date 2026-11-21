package mx.edu.uttt.rutitas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.rutitas.network.BackendErrorHandler
import mx.edu.uttt.rutitas.network.BackendRouteDto
import mx.edu.uttt.rutitas.network.BackendRouteDetailDto
import mx.edu.uttt.rutitas.repository.BackendRoutesRepository
import javax.inject.Inject

data class BackendRoutesUiState(
    val loading: Boolean = false,
    val detailLoading: Boolean = false,
    val search: String = "",
    val page: Int = 1,
    val totalPages: Long = 0L,
    val total: Long = 0L,
    val routes: List<BackendRouteDto> = emptyList(),
    val detail: BackendRouteDetailDto? = null,
    val error: String? = null
)

@HiltViewModel
class BackendRoutesViewModel @Inject constructor(
    private val repository: BackendRoutesRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(BackendRoutesUiState())
    val ui = _ui.asStateFlow()

    init {
        loadRoutes()
    }

    fun changeSearch(value: String) {
        _ui.value = _ui.value.copy(search = value)
    }

    fun searchRoutes() {
        loadRoutes(1)
    }

    fun loadRoutes(page: Int = _ui.value.page) {
        if (_ui.value.loading || _ui.value.detailLoading) return
        if (page < 1) return

        val query = _ui.value.search.trim().ifBlank { null }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                loading = true,
                error = null,
                detail = null
            )

            try {
                val result = repository.getRoutes(
                    page = page,
                    pageSize = 20,
                    query = query
                )

                _ui.value = _ui.value.copy(
                    loading = false,
                    routes = result.items,
                    page = result.page,
                    total = result.total,
                    totalPages = result.totalPages,
                    error = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = BackendErrorHandler.from(e).message
                )
            }
        }
    }

    fun openRoute(id: String) {
        if (_ui.value.loading || _ui.value.detailLoading) return

        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                detailLoading = true,
                detail = null,
                error = null
            )

            try {
                val route = repository.getRouteById(id)

                _ui.value = _ui.value.copy(
                    detailLoading = false,
                    detail = route
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    detailLoading = false,
                    error = BackendErrorHandler.from(e).message
                )
            }
        }
    }

    fun closeDetail() {
        _ui.value = _ui.value.copy(
            detail = null,
            error = null
        )
    }
}
