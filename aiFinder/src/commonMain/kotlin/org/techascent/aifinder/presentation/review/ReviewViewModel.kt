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
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.inventory_save_error
import apphub.aifinder.generated.resources.scan_existing_items_error
import apphub.aifinder.generated.resources.scan_existing_items_retry_error
import org.jetbrains.compose.resources.getString

class ReviewViewModel(
    private val saveDetections: SaveDetectionsUseCase,
    private val observeInventory: ObserveInventoryUseCase? = null,
) : ViewModel() {
    private var detections: List<ItemDetection> = emptyList()
    private var photo: CapturedPhoto? = null
    private var existing = emptyList<StoredItem>()
    private var locationId: String? = null
    private var observeJob: Job? = null
    private var choices = emptyMap<String, RescanChoice>()
    private var checkingExisting = false
    private var existingError: String? = null

    fun setLocation(id: String?) {
        if (isSaving) return
        locationId = id
        choices = emptyMap()
        publish()
        if (observeJob?.isActive == true || observeInventory == null) return
        checkingExisting = true
        existingError = null
        publish()
        observeJob = viewModelScope.launch {
            val refreshed = observeInventory.refreshItems()
            if (refreshed.isFailure) {
                checkingExisting = false
                existingError = getString(Res.string.scan_existing_items_error)
                publish()
                return@launch
            }
            observeInventory.observeItems().catch {
                checkingExisting = false
                existingError = getString(Res.string.scan_existing_items_retry_error)
                publish(saveError = existingError)
            }.collect {
                existing = it
                checkingExisting = false
                publish(isSaving = isSaving)
            }
        }
    }

    private fun matches(): Map<String, StoredItem> = detections.mapNotNull { detection ->
        val candidates = existing.filter {
            it.locationId == locationId && it.item.name.trim().equals(detection.item.name.trim(), ignoreCase = true)
        }
        candidates.firstOrNull()?.let { detection.item.id to it }
    }.toMap()

    private fun ambiguousMatches(): Set<String> = detections.filter { detection ->
        existing.count { it.locationId == locationId &&
            it.item.name.trim().equals(detection.item.name.trim(), ignoreCase = true) } > 1
    }.map { it.item.id }.toSet()

    fun choose(itemId: String, choice: RescanChoice) {
        if (isSaving || itemId !in matches() || choice == RescanChoice.UPDATE && itemId in ambiguousMatches()) return
        choices = choices + (itemId to choice)
        publish()
    }

    var state: ReviewUiState by mutableStateOf(ReviewUiState.Success(ReviewContent(detections)))
        private set

    private val isSaving get() = (state as? ReviewUiState.Success)?.data?.isSaving == true

    fun show(newDetections: List<ItemDetection>, newPhoto: CapturedPhoto? = null) {
        observeJob?.cancel()
        observeJob = null
        existing = emptyList()
        existingError = null
        checkingExisting = observeInventory != null && newDetections.isNotEmpty()
        locationId = null
        detections = newDetections
        photo = newPhoto
        choices = emptyMap()
        publish()
    }

    fun rename(itemId: String, name: String) {
        if (isSaving) return
        val trimmedName = name.trim()
        check(trimmedName.isNotEmpty())
        detections = detections.map { detection ->
            if (detection.item.id == itemId) {
                detection.copy(item = detection.item.copy(name = trimmedName))
            } else {
                detection
            }
        }
        choices = choices - itemId
        publish()
    }

    fun delete(itemId: String) {
        if (isSaving) return
        detections = detections.filterNot { it.item.id == itemId }
        publish()
    }

    /** Saves the reviewed items into [locationId]; [onSaved] runs once they are in Firebase and the cache. */
    fun save(locationId: String?, onSaved: () -> Unit) {
        if (observeInventory != null && locationId != this.locationId) setLocation(locationId)
        if (isSaving || checkingExisting || existingError != null) return
        publish(isSaving = true)
        viewModelScope.launch {
            val matched = matches()
            val selected = detections.filter {
                it.item.id !in matched || (choices[it.item.id] ?: RescanChoice.KEEP) != RescanChoice.KEEP
            }
            if (selected.isEmpty() && matched.isNotEmpty()) {
                reset()
                onSaved()
                return@launch
            }
            val replacements = matched.filterKeys { choices[it] == RescanChoice.UPDATE }
            saveDetections(locationId, selected, photo, replacements)
                .onSuccess {
                    reset()
                    onSaved()
                }
                .onFailure { e ->
                    publish(saveError = e.message ?: getString(Res.string.inventory_save_error))
                }
        }
    }

    fun retry() {
        if (existingError != null) {
            observeJob?.cancel()
            observeJob = null
            setLocation(locationId)
        } else publish()
    }

    fun reset() {
        observeJob?.cancel()
        observeJob = null
        existing = emptyList()
        checkingExisting = false
        existingError = null
        locationId = null
        show(emptyList())
    }

    private fun publish(isSaving: Boolean = false, saveError: String? = null) {
        state = ReviewUiState.Success(ReviewContent(
            detections, isSaving, saveError ?: existingError, matches(), choices, checkingExisting,
            ambiguousMatches(), photo,
        ))
    }
}
