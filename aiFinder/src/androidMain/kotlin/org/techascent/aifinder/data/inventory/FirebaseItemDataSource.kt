package org.techascent.aifinder.data.inventory

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig
import org.techascent.aifinder.data.location.RealtimeDatabasePaths

class FirebaseItemDataSource(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
) : ItemRemoteDataSource {
    override suspend fun clearPhotos(userId: String, itemIds: List<String>) {
        database.getReference(RealtimeDatabasePaths.items(userId)).updateChildren(
            itemIds.flatMap { listOf("$it/photoId" to null, "$it/bounds" to null) }.toMap()
        ).await()
    }
    override suspend fun writeAll(userId: String, items: List<Pair<String, ItemDto>>) {
        database.getReference(RealtimeDatabasePaths.items(userId))
            .updateChildren(items.associate { it.first to it.second.toMap() }).await()
    }
    override suspend fun update(userId: String, itemId: String, item: ItemDto) {
        database.getReference(RealtimeDatabasePaths.items(userId)).child(itemId).setValue(item.toMap()).await()
    }

    override suspend fun delete(userId: String, itemId: String) {
        database.getReference(RealtimeDatabasePaths.items(userId)).child(itemId).removeValue().await()
    }

    override suspend fun fetchAll(userId: String): List<Pair<String, ItemDto>> =
        database.getReference(RealtimeDatabasePaths.items(userId)).get().await()
            .children.mapNotNull { child ->
                val id = child.key ?: return@mapNotNull null
                val dto = (child.value as? Map<*, *>)?.let(ItemDto::fromMap) ?: return@mapNotNull null
                id to dto
            }

    override suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>> {
        val ref = database.getReference(RealtimeDatabasePaths.items(userId))
        val created = items.map { item ->
            checkNotNull(ref.push().key) { "Realtime Database didn't generate a key." } to item
        }
        ref.updateChildren(created.associate { (id, item) -> id to item.toMap() }).await()
        return created
    }
}
