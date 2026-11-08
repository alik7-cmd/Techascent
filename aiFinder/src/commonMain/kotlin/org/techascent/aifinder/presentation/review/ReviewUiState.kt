package org.techascent.aifinder.presentation.review

import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.inventory.StoredItem

enum class RescanChoice { KEEP, UPDATE, ADD }

data class ReviewContent(
    val detections: List<ItemDetection>,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val matches: Map<String, StoredItem> = emptyMap(),
    val choices: Map<String, RescanChoice> = emptyMap(),
    val checkingExisting: Boolean = false,
    val ambiguousMatches: Set<String> = emptySet(),
    val photo: org.techascent.aifinder.domain.vision.CapturedPhoto? = null,
)

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data class Success(val data: ReviewContent) : ReviewUiState
    data class Error(val message: String) : ReviewUiState
}
