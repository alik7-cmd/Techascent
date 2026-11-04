package org.techascent.aifinder.presentation.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.presentation.inventory.InventoryBackedViewModel
import org.techascent.aifinder.presentation.inventory.InventoryLoad
import org.techascent.aifinder.presentation.spaces.SpaceListEntry

class HomeViewModel(observeInventory: ObserveInventoryUseCase) :
    InventoryBackedViewModel(observeInventory) {
    val uiState = inventory.map { load ->
        when (load) {
            InventoryLoad.Loading -> HomeUiState.Loading
            is InventoryLoad.Failed -> HomeUiState.Error(load.message)
            is InventoryLoad.Ready -> HomeUiState.Success(
                HomeContent(load.snapshot.allSpaces.map { space ->
                    val parentPath =
                        space.parentId?.let(load.snapshot::pathOf)?.takeIf { it.isNotEmpty() }
                    SpaceListEntry(space, parentPath)
                }.take(3))
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState.Loading)
}
