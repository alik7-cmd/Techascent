package org.techascent.aifinder.presentation.search

import org.techascent.aifinder.domain.model.LocatedItem

data class SearchContent(val query: String, val results: List<LocatedItem>, val hasItems: Boolean = results.isNotEmpty())

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val data: SearchContent) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
