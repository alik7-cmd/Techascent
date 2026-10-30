package org.techascent.aifinder.presentation.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.techascent.aifinder.data.*

class ReviewViewModel(private val repository: InventoryRepository = MockInventoryRepository()) : ViewModel() {
    var state: ReviewUiState by mutableStateOf(ReviewUiState.Success(ReviewContent(repository.detections())))
        private set
    fun retry() { state = ReviewUiState.Success(ReviewContent(repository.detections())) }
}
