package org.techascent.aifinder.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.techascent.aifinder.data.*

class SearchViewModel(private val repository: InventoryRepository = MockInventoryRepository()) : ViewModel() {
    var state: SearchUiState by mutableStateOf(SearchUiState.Success(SearchContent("", repository.items())))
        private set
    fun retry() { state = SearchUiState.Success(SearchContent("", repository.items())) }

    fun updateQuery(query: String) { state = SearchUiState.Success(SearchContent(query, repository.search(query))) }
}
