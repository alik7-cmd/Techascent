package org.techascent.aifinder.presentation.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.inventory.usecase.SaveDetectionsUseCase
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.vision.CapturedPhoto

class ReviewViewModel(private val saveDetections: SaveDetectionsUseCase) : ViewModel() {
    private var detections: List<ItemDetection> = emptyList()
    private var photo: CapturedPhoto? = null

    var state: ReviewUiState by mutableStateOf(ReviewUiState.Success(ReviewContent(detections)))
        private set

    private val isSaving get() = (state as? ReviewUiState.Success)?.data?.isSaving == true

    fun show(newDetections: List<ItemDetection>, newPhoto: CapturedPhoto? = null) {
        detections = newDetections
        photo = newPhoto
        publish()
    }

    fun rename(itemId: String, name: String) {
        if (isSaving) return
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Item name must not be blank." }
        detections = detections.map { detection ->
            if (detection.item.id == itemId) {
                detection.copy(item = detection.item.copy(name = trimmedName))
            } else {
                detection
            }
        }
        publish()
    }

    fun delete(itemId: String) {
        if (isSaving) return
        detections = detections.filterNot { it.item.id == itemId }
        publish()
    }

    /** Saves the reviewed items into [locationId]; [onSaved] runs once they are in Firebase and the cache. */
    fun save(locationId: String?, onSaved: () -> Unit) {
        if (isSaving) return
        publish(isSaving = true)
        viewModelScope.launch {
            saveDetections(locationId, detections, photo)
                .onSuccess {
                    detections = emptyList()
                    photo = null
                    publish()
                    onSaved()
                }
                .onFailure { e -> publish(saveError = e.message ?: "Couldn't save your items.") }
        }
    }

    fun retry() = publish()

    private fun publish(isSaving: Boolean = false, saveError: String? = null) {
        state = ReviewUiState.Success(ReviewContent(detections, isSaving, saveError))
    }
}
