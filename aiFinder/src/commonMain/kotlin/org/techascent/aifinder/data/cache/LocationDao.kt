package org.techascent.aifinder.data.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LocationDao {
    @Query("SELECT * FROM CachedLocation WHERE userId = :userId ORDER BY createdAt, id")
    abstract fun observe(userId: String): Flow<List<CachedLocation>>

    @Query("SELECT COUNT(*) FROM CachedLocation WHERE userId = :userId")
    abstract suspend fun count(userId: String): Long

    @Upsert
    abstract suspend fun upsert(location: CachedLocation)

    @Upsert
    abstract suspend fun upsertAll(locations: List<CachedLocation>)

    @Query("DELETE FROM CachedLocation WHERE userId = :userId")
    abstract suspend fun deleteForUser(userId: String)

    @Transaction
    open suspend fun replaceAll(userId: String, locations: List<CachedLocation>) {
        deleteForUser(userId)
        upsertAll(locations)
    }
}
