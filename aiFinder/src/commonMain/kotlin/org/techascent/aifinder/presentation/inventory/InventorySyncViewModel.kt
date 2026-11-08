package org.techascent.aifinder.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.util.logDebug

data class InventorySyncState(val busy: Boolean = false, val failed: Boolean = false)

class InventorySyncViewModel(
    private val items: InventoryRepository,
    private val locations: LocationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InventorySyncState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    fun refresh() {
        if (job?.isActive == true) return
        _state.value = InventorySyncState(busy = true)
        job = viewModelScope.launch {
            val places = locations.refresh()
            val inventory = if (places.isSuccess) items.refresh() else places
            val failure = places.exceptionOrNull() ?: inventory.exceptionOrNull()
            failure?.let { logDebug("InventorySync", "Refresh failed: ${it.message}") }
            _state.value = InventorySyncState(failed = failure != null)
        }
    }
    fun reset() { job?.cancel(); job = null; _state.value = InventorySyncState() }
}
