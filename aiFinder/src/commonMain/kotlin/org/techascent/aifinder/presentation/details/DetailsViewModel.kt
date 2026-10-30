package org.techascent.aifinder.presentation.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.techascent.aifinder.data.*

class DetailsViewModel(private val repository: InventoryRepository = MockInventoryRepository()) : ViewModel() {
    var state: DetailsUiState by mutableStateOf(DetailsUiState.Success(DetailsContent(repository.items().firstOrNull())))
        private set
    fun select(id: String) { state = DetailsUiState.Success(DetailsContent(repository.items().firstOrNull { it.item.id == id })) }
    fun retry() { state = DetailsUiState.Success(DetailsContent(repository.items().firstOrNull())) }
}
