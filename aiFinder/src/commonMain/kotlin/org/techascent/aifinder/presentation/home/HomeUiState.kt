package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.presentation.spaces.SpaceListEntry
import org.techascent.aifinder.domain.inventory.InventorySnapshot
import org.techascent.aifinder.domain.model.LocatedItem

data class HomeContent(
    val spaces: List<SpaceListEntry>,
    val placeCount: Int,
    val itemCount: Int,
    val recentItems: List<LocatedItem>,
)

internal fun InventorySnapshot.toHomeContent(): HomeContent {
    val latestPhotos = items.filter { it.item.photoId != null }.groupBy { it.location.id }
        .mapValues { (_, entries) -> entries.maxByOrNull { it.createdAt }?.item?.photoId }
    return HomeContent(
        spaces = allSpaces.take(3).map { space ->
            SpaceListEntry(
                space,
                space.parentId?.let(::pathOf)?.takeIf(String::isNotEmpty),
                latestPhotos[space.id],
            )
        },
        placeCount = locations.size,
        itemCount = items.size,
        recentItems = items.sortedWith(compareByDescending<LocatedItem> { it.createdAt }
            .thenBy { it.item.id }).take(3),
    )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
