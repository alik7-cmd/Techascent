package org.techascent.shared.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.techascent.shared.data.PrayerTimeMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse

@Entity(tableName = "prayer_times")
data class PrayerTimesEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // single entry
    val response: PrayerTimeMonthlyResponse
)

