package org.techascent.aifinder.presentation.home

data class HomeContent(val spaces: List<org.techascent.aifinder.domain.model.StorageLocation>)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
