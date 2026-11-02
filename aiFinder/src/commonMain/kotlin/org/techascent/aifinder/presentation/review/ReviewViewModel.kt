package org.techascent.aifinder.presentation.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.model.StorageLocation
import org.techascent.aifinder.data.InventoryRepository
import org.techascent.aifinder.data.MockInventoryRepository

class ReviewViewModel(private val repository: InventoryRepository = MockInventoryRepository()) : ViewModel() {
    private var detections: List<ItemDetection> = emptyList()

    var state: ReviewUiState by mutableStateOf(ReviewUiState.Success(ReviewContent(detections)))
        private set

    fun show(newDetections: List<ItemDetection>) {
        detections = newDetections
        state = ReviewUiState.Success(ReviewContent(detections))
    }

    fun rename(itemId: String, name: String) {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Item name must not be blank." }
        detections = detections.map { detection ->
            if (detection.item.id == itemId) {
                detection.copy(item = detection.item.copy(name = trimmedName))
            } else {
                detection
            }
        }
        state = ReviewUiState.Success(ReviewContent(detections))
    }

    fun delete(itemId: String) {
        detections = detections.filterNot { it.item.id == itemId }
        state = ReviewUiState.Success(ReviewContent(detections))
    }

    fun save(location: StorageLocation, path: String) {
        repository.saveDetections(location, path, detections)
    }

    fun retry() { state = ReviewUiState.Success(ReviewContent(detections)) }
}
