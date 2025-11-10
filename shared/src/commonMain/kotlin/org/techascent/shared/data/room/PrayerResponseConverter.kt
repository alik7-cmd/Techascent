package org.techascent.shared.data.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.techascent.shared.data.PrayerTimesResponse

class PrayerResponseConverter {
    @TypeConverter
    fun fromResponse(value: PrayerTimesResponse): String = Json.encodeToString(value)

    @TypeConverter
    fun toResponse(value: String): PrayerTimesResponse = Json.decodeFromString(value)
}
