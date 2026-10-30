package org.techascent.aifinder.presentation.search

data class SearchContent(val query: String, val results: List<org.techascent.aifinder.domain.model.LocatedItem>)

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val data: SearchContent) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
