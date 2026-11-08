package org.techascent.aifinder.data.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ItemDao {
    @Query("SELECT * FROM CachedItem WHERE userId = :userId ORDER BY createdAt DESC, id")
    abstract fun observe(userId: String): Flow<List<CachedItem>>

    @Query("SELECT COUNT(*) FROM CachedItem WHERE userId = :userId")
    abstract suspend fun count(userId: String): Long

    @Upsert
    abstract suspend fun upsertAll(items: List<CachedItem>)

    @Query("DELETE FROM CachedItem WHERE userId = :userId")
    abstract suspend fun deleteForUser(userId: String)

    @Query("DELETE FROM CachedItem WHERE userId = :userId AND id = :id")
    abstract suspend fun delete(userId: String, id: String)

    @Transaction
    open suspend fun replaceAll(userId: String, items: List<CachedItem>) {
        deleteForUser(userId)
        upsertAll(items)
    }
}
