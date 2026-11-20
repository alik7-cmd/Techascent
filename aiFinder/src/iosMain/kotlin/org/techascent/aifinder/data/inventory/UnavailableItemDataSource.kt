package org.techascent.aifinder.data.inventory

import org.techascent.aifinder.domain.inventory.InventoryError

// iOS needs GoogleService-Info.plist and the Firebase iOS SDK before Realtime Database can be used.
class UnavailableItemDataSource : ItemRemoteDataSource {
    override suspend fun fetchAll(userId: String): List<Pair<String, ItemDto>> = throw InventoryError.Unavailable

    override suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>> =
        throw InventoryError.Unavailable
}
