package org.techascent.aifinder.domain.inventory

import kotlinx.coroutines.flow.Flow
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection

/** An item the signed-in user saved into one of their locations. */
data class StoredItem(
    val item: InventoryItem,
    val locationId: String,
    val createdAt: Long,
)

interface InventoryRepository {
    /** Emits the signed-in user's saved items, newest first, and keeps emitting as the cache changes. */
    fun observeItems(): Flow<List<StoredItem>>

    suspend fun saveDetections(locationId: String, detections: List<ItemDetection>): Result<List<StoredItem>>
}

sealed class InventoryError(message: String) : Exception(message) {
    data object NothingToSave : InventoryError("There are no items to save.")
    data object NoLocation : InventoryError("Choose a location before saving.")
    data object NotSignedIn : InventoryError("Sign in to sync your items.")
    data object Unavailable : InventoryError("Item sync isn't available on this device yet.")
    class Remote(message: String?) : InventoryError(message ?: "Couldn't reach the database. Try again.")
}
