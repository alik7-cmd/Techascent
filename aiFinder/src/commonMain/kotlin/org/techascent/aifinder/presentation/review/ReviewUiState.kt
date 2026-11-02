package org.techascent.aifinder.presentation.review

import org.techascent.aifinder.domain.model.ItemDetection

data class ReviewContent(
    val detections: List<ItemDetection>,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data class Success(val data: ReviewContent) : ReviewUiState
    data class Error(val message: String) : ReviewUiState
}
