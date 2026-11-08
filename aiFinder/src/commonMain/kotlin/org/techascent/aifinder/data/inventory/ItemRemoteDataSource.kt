package org.techascent.aifinder.data.inventory

interface ItemRemoteDataSource {
    /** One-shot read of every `(id, dto)` the user owns. */
    suspend fun fetchAll(userId: String): List<Pair<String, ItemDto>>

    /** Writes all items in one atomic update and returns them with their generated ids, in order. */
    suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>>
    suspend fun update(userId: String, itemId: String, item: ItemDto) {
        error("Updating items is unavailable.")
    }
    suspend fun delete(userId: String, itemId: String) {
        error("Deleting items is unavailable.")
    }
    suspend fun writeAll(userId: String, items: List<Pair<String, ItemDto>>) {
        error("Saving reviewed items is unavailable.")
    }
    suspend fun clearPhotos(userId: String, itemIds: List<String>) {
        error("Removing photo references is unavailable.")
    }
}
