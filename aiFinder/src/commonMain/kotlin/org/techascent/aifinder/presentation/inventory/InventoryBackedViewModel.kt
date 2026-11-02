package org.techascent.aifinder.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.inventory.InventorySnapshot
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase

sealed interface InventoryLoad {
    data object Loading : InventoryLoad
    data class Ready(val snapshot: InventorySnapshot) : InventoryLoad
    data class Failed(val message: String) : InventoryLoad
}

/** Observes the signed-in user's cached inventory; started lazily because sign-in can happen later. */
abstract class InventoryBackedViewModel(
    private val observeInventory: ObserveInventoryUseCase,
) : ViewModel() {
    private val _inventory = MutableStateFlow<InventoryLoad>(InventoryLoad.Loading)
    protected val inventory: StateFlow<InventoryLoad> = _inventory.asStateFlow()
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        if (_inventory.value is InventoryLoad.Failed) _inventory.value = InventoryLoad.Loading
        job = viewModelScope.launch {
            observeInventory()
                .catch { e -> _inventory.value = InventoryLoad.Failed(e.message ?: "Couldn't load your items.") }
                .collect { _inventory.value = InventoryLoad.Ready(it) }
        }
    }

    fun retry() {
        job?.cancel()
        job = null
        _inventory.value = InventoryLoad.Loading
        start()
    }

    /** Drops the previous user's data after sign-out. */
    open fun reset() {
        job?.cancel()
        job = null
        _inventory.value = InventoryLoad.Loading
    }
}
