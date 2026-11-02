package org.techascent.aifinder.data.cache

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

@Entity
data class WelcomePreference(
    @PrimaryKey val id: Int = 1,
    val seen: Boolean = true,
)

@Dao
interface WelcomePreferenceDao {
    @Query("SELECT seen FROM WelcomePreference WHERE id = 1")
    suspend fun hasSeenWelcome(): Boolean?

    @Upsert
    suspend fun upsert(preference: WelcomePreference)
}
