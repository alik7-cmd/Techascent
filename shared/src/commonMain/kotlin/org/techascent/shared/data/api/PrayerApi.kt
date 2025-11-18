package org.techascent.shared.data.api

import org.techascent.shared.data.PrayerTimesMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse


interface PrayerApi {
    suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        school: Int
    ): PrayerTimesResponse

    suspend fun getMonthlyPrayerTimes(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        school: Int
    ): PrayerTimesMonthlyResponse
}