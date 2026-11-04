package org.techascent.aifinder.data.cache

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

@Entity
data class CachedScanWallet(
    @PrimaryKey val userId: String,
    val remainingScans: Long,
)

@Dao
interface ScanWalletDao {
    @Query("SELECT * FROM CachedScanWallet WHERE userId = :userId")
    suspend fun get(userId: String): CachedScanWallet?

    @Upsert
    suspend fun upsert(wallet: CachedScanWallet)

    @Query("DELETE FROM CachedScanWallet WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}
