package org.techascent.aifinder.presentation.location

import org.techascent.aifinder.domain.location.pathOf
import org.techascent.aifinder.domain.model.StorageLocation

data class LocationUiState(
    val locations: List<StorageLocation> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val selectedId: String? = null,
    val isCreating: Boolean = false,
    val createError: String? = null,
) {
    val selectedLocation: StorageLocation? get() = selectedId?.let { id -> locations.firstOrNull { it.id == id } }
    val selectedPath: String get() = selectedId?.let(::path).orEmpty()

    fun path(id: String): String = locations.pathOf(id)
}
