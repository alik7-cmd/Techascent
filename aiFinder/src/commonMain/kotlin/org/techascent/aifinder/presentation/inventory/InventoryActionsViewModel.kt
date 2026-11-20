package org.techascent.aifinder.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.inventory_update_error
import apphub.aifinder.generated.resources.item_name_required
import apphub.aifinder.generated.resources.item_place_required
import org.jetbrains.compose.resources.getString
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.model.LocatedItem
import kotlin.time.Clock

data class InventoryActionState(val busy: Boolean = false, val error: String? = null)

@OptIn(kotlin.time.ExperimentalTime::class)
class InventoryActionsViewModel(
    private val repository: InventoryRepository,
    private val photos: org.techascent.aifinder.domain.photo.ItemPhotoRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InventoryActionState())
    val state = _state.asStateFlow()
    private var actionJob: kotlinx.coroutines.Job? = null

    fun reset() {
        actionJob?.cancel()
        actionJob = null
        _state.value = InventoryActionState()
    }

    fun clearError() { if (!_state.value.busy) _state.value = InventoryActionState() }

    fun save(entry: LocatedItem?, item: InventoryItem, locationId: String, onSaved: () -> Unit) = perform(onSaved) {
        if (item.name.isBlank()) throw IllegalArgumentException(getString(Res.string.item_name_required))
        if (locationId.isBlank()) throw IllegalArgumentException(getString(Res.string.item_place_required))
        val now = Clock.System.now().toEpochMilliseconds()
        if (entry == null) {
            repository.saveDetections(locationId, listOf(ItemDetection("", item))).map { Unit }
        } else {
            repository.update(StoredItem(
                item, locationId, entry.createdAt,
                if (locationId != entry.location.id) now else entry.confirmedAt, entry.bounds,
            ))
        }
    }

    fun confirm(entry: LocatedItem) = perform({}) {
        repository.update(StoredItem(
            entry.item, entry.location.id, entry.createdAt, Clock.System.now().toEpochMilliseconds(), entry.bounds,
        ))
    }

    fun delete(entry: LocatedItem, onDeleted: () -> Unit) = perform(onDeleted) {
        repository.delete(entry.item.id)
    }

    fun deletePhoto(entry: LocatedItem) = perform({}) {
        val id = entry.item.photoId ?: return@perform Result.success(Unit)
        photos.delete(id).fold(
            onSuccess = { repository.clearPhotoReferences(id) },
            onFailure = { Result.failure(it) },
        )
    }

    private fun perform(onSuccess: () -> Unit, action: suspend () -> Result<Unit>) {
        if (_state.value.busy) return
        _state.value = InventoryActionState(busy = true)
        actionJob = viewModelScope.launch {
            try {
                val result = action()
                result.onSuccess {
                    _state.value = InventoryActionState()
                    onSuccess()
                }.onFailure {
                    _state.value = InventoryActionState(
                        error = it.message ?: getString(Res.string.inventory_update_error),
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _state.value = InventoryActionState(error = e.message)
            }
        }
    }
}
