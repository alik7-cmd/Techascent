package org.techascent.aifinder.presentation.spaces

import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.domain.model.StorageLocation

data class SpaceListEntry(val location: StorageLocation, val parentPath: String?)

sealed interface SpacesUiState {
    data object Loading : SpacesUiState
    data class Success(val spaces: List<SpaceListEntry>) : SpacesUiState
    data class Error(val message: String) : SpacesUiState
}

/** [location] is null when the selected space was removed. */
data class SpaceContent(val location: StorageLocation?, val path: String, val items: List<LocatedItem>)

sealed interface SpaceUiState {
    data object Loading : SpaceUiState
    data class Success(val data: SpaceContent) : SpaceUiState
    data class Error(val message: String) : SpaceUiState
}
