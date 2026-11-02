package org.techascent.aifinder.data.inventory

interface ItemRemoteDataSource {
    /** One-shot read of every `(id, dto)` the user owns. */
    suspend fun fetchAll(userId: String): List<Pair<String, ItemDto>>

    /** Writes all items in one atomic update and returns them with their generated ids, in order. */
    suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>>
}
