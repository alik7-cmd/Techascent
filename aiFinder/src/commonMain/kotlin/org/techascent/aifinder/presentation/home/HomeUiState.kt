package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.domain.model.StorageLocation

data class HomeContent(val spaces: List<StorageLocation>)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
