package org.techascent.shared.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrayerTimesDao {
    @Query("SELECT * FROM prayer_times WHERE id = 1")
    suspend fun getPrayerTimes(): PrayerTimesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(entity: PrayerTimesEntity)
}
