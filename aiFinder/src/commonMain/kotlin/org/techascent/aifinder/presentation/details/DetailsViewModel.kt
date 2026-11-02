package org.techascent.aifinder.presentation.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.presentation.inventory.InventoryBackedViewModel
import org.techascent.aifinder.presentation.inventory.InventoryLoad

class DetailsViewModel(observeInventory: ObserveInventoryUseCase) : InventoryBackedViewModel(observeInventory) {
    private val selectedId = MutableStateFlow<String?>(null)

    val state = combine(inventory, selectedId) { load, id ->
        when (load) {
            InventoryLoad.Loading -> DetailsUiState.Loading
            is InventoryLoad.Failed -> DetailsUiState.Error(load.message)
            is InventoryLoad.Ready -> DetailsUiState.Success(DetailsContent(id?.let(load.snapshot::item)))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DetailsUiState.Loading)

    fun select(id: String) { selectedId.value = id }

    override fun reset() {
        super.reset()
        selectedId.value = null
    }
}
