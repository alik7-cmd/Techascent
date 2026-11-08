package org.techascent.aifinder.data.inventory

import kotlinx.coroutines.flow.Flow

/** Device cache of a user's items; the source the UI reads from. */
interface ItemLocalDataSource {
    fun observe(userId: String): Flow<List<Pair<String, ItemDto>>>
    suspend fun isEmpty(userId: String): Boolean
    suspend fun replaceAll(userId: String, items: List<Pair<String, ItemDto>>)
    suspend fun upsertAll(userId: String, items: List<Pair<String, ItemDto>>)
    suspend fun delete(userId: String, itemId: String) {
        error("Deleting cached items is unavailable.")
    }
}
