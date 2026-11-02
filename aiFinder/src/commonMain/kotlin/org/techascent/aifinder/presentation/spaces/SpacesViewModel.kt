package org.techascent.aifinder.presentation.spaces

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.presentation.inventory.InventoryBackedViewModel
import org.techascent.aifinder.presentation.inventory.InventoryLoad

class SpacesViewModel(observeInventory: ObserveInventoryUseCase) : InventoryBackedViewModel(observeInventory) {
    private val selectedId = MutableStateFlow<String?>(null)

    val allSpaces = inventory.map { load ->
        when (load) {
            InventoryLoad.Loading -> SpacesUiState.Loading
            is InventoryLoad.Failed -> SpacesUiState.Error(load.message)
            is InventoryLoad.Ready -> SpacesUiState.Success(
                load.snapshot.allSpaces.map { space ->
                    val parentPath = space.parentId?.let(load.snapshot::pathOf)?.takeIf { it.isNotEmpty() }
                    SpaceListEntry(space, parentPath)
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SpacesUiState.Loading)

    val space = combine(inventory, selectedId) { load, id ->
        when (load) {
            InventoryLoad.Loading -> SpaceUiState.Loading
            is InventoryLoad.Failed -> SpaceUiState.Error(load.message)
            is InventoryLoad.Ready -> {
                val location = id?.let(load.snapshot::location)
                SpaceUiState.Success(
                    SpaceContent(
                        location = location,
                        path = location?.let { load.snapshot.pathOf(it.id) }.orEmpty(),
                        items = location?.let { load.snapshot.itemsIn(it.id) }.orEmpty(),
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SpaceUiState.Loading)

    fun select(id: String) { selectedId.value = id }

    override fun reset() {
        super.reset()
        selectedId.value = null
    }
}
