package org.techascent.aifinder.presentation.location

import org.techascent.aifinder.domain.location.pathOf
import org.techascent.aifinder.domain.model.StorageLocation
import org.techascent.aifinder.domain.model.LocationType

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

    fun ancestors(id: String?): List<StorageLocation> {
        val result = mutableListOf<StorageLocation>()
        val visited = mutableSetOf<String>()
        var current = locations.firstOrNull { it.id == id }
        while (current != null && visited.add(current.id)) {
            if (current.type != LocationType.HOME) result.add(0, current)
            val parentId = current.parentId
            current = locations.firstOrNull { it.id == parentId }
        }
        return result
    }

    fun placesInside(id: String?): List<StorageLocation> = locations.filter { location ->
        location.type != LocationType.HOME &&
            ancestors(location.id).dropLast(1).lastOrNull()?.id == id
    }.sortedBy { it.name.lowercase() }
}
