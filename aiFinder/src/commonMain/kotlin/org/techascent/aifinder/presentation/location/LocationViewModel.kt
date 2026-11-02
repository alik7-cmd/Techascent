package org.techascent.aifinder.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.location.usecase.CreateLocationUseCase
import org.techascent.aifinder.domain.location.usecase.ObserveLocationsUseCase
import org.techascent.aifinder.domain.model.CreateLocationRequest

class LocationViewModel(
    private val observeLocations: ObserveLocationsUseCase,
    private val createLocation: CreateLocationUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LocationUiState())
    val state = _state.asStateFlow()

    private var observeJob: Job? = null

    /** Starts listening lazily, since the user may not be signed in when this ViewModel is created. */
    fun start() {
        if (observeJob?.isActive == true) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        observeJob = viewModelScope.launch {
            observeLocations()
                .catch { e -> _state.update { it.copy(isLoading = false, loadError = e.message) } }
                .collect { locations ->
                    _state.update { current ->
                        current.copy(
                            locations = locations,
                            isLoading = false,
                            loadError = null,
                            selectedId = current.selectedId?.takeIf { id -> locations.any { it.id == id } },
                        )
                    }
                }
        }
    }

    /** Drops the previous user's locations after sign-out. */
    fun reset() {
        observeJob?.cancel()
        observeJob = null
        _state.value = LocationUiState()
    }

    fun retry() {
        observeJob?.cancel()
        start()
    }

    fun select(id: String) {
        require(_state.value.locations.any { it.id == id }) { "Location not found." }
        _state.update { it.copy(selectedId = id) }
    }

    fun clearError() = _state.update { it.copy(createError = null) }

    fun create(request: CreateLocationRequest, onCreated: () -> Unit = {}) {
        if (_state.value.isCreating) return
        _state.update { it.copy(isCreating = true, createError = null) }
        viewModelScope.launch {
            createLocation(request, _state.value.locations)
                .onSuccess { created ->
                    _state.update { current ->
                        current.copy(
                            // The listener usually delivers this first; keep the list consistent either way.
                            locations = if (current.locations.any { it.id == created.id }) current.locations
                            else current.locations + created,
                            selectedId = created.id,
                            isCreating = false,
                        )
                    }
                    onCreated()
                }
                .onFailure { e -> _state.update { it.copy(isCreating = false, createError = e.message) } }
        }
    }
}
