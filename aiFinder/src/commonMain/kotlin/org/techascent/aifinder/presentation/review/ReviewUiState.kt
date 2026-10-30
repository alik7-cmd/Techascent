package org.techascent.aifinder.presentation.review

data class ReviewContent(val detections: List<org.techascent.aifinder.domain.model.ItemDetection>)

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data class Success(val data: ReviewContent) : ReviewUiState
    data class Error(val message: String) : ReviewUiState
}
