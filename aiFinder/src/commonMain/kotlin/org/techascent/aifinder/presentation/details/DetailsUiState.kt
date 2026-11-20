package org.techascent.aifinder.presentation.details

data class DetailsContent(val entry: org.techascent.aifinder.domain.model.LocatedItem?)

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val data: DetailsContent) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}
