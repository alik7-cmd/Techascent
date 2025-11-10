package org.techascent.shared.data.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.techascent.shared.data.PrayerTimeMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse

class PrayerResponseConverter {
    @TypeConverter
    fun fromResponse(value: PrayerTimeMonthlyResponse): String = Json.encodeToString(value)

    @TypeConverter
    fun toResponse(value: String): PrayerTimeMonthlyResponse = Json.decodeFromString(value)
}
