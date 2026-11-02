package org.techascent.aifinder.presentation.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.presentation.inventory.InventoryBackedViewModel
import org.techascent.aifinder.presentation.inventory.InventoryLoad

class SearchViewModel(observeInventory: ObserveInventoryUseCase) : InventoryBackedViewModel(observeInventory) {
    private val query = MutableStateFlow("")

    val state = combine(inventory, query) { load, query ->
        when (load) {
            InventoryLoad.Loading -> SearchUiState.Loading
            is InventoryLoad.Failed -> SearchUiState.Error(load.message)
            is InventoryLoad.Ready -> SearchUiState.Success(
                SearchContent(query, load.snapshot.search(query), hasItems = load.snapshot.items.isNotEmpty())
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState.Loading)

    fun updateQuery(value: String) { query.value = value }

    fun clearQuery() { query.value = "" }

    override fun reset() {
        super.reset()
        clearQuery()
    }
}
