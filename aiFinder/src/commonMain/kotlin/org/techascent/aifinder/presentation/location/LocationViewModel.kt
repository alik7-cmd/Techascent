package org.techascent.aifinder.presentation.location

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.techascent.aifinder.data.InventoryRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType

class LocationViewModel(private val repository: InventoryRepository) : ViewModel() {
    var locations by mutableStateOf(repository.locations())
        private set
    var selectedId by mutableStateOf(locations.firstOrNull { it.type == LocationType.DRAWER }?.id)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    val selectedPath: String get() = selectedId?.let(repository::locationPath).orEmpty()

    fun path(id: String): String = repository.locationPath(id)

    fun select(id: String) {
        require(locations.any { it.id == id }) { "Location not found." }
        selectedId = id
    }

    fun clearError() { error = null }

    fun create(request: CreateLocationRequest): Boolean {
        return try {
            val created = repository.createLocation(request)
            locations = repository.locations()
            selectedId = created.id
            error = null
            true
        } catch (e: IllegalArgumentException) {
            error = e.message
            false
        }
    }
}
