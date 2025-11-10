package org.techascent.shared.data.api

import org.techascent.shared.data.PrayerTimesResponse


interface PrayerApi {
    suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        school: Int
    ): PrayerTimesResponse

    suspend fun getPrayerTimesByMonth(
        year: Int,
        month: Int,
        city: String,
        country: String,
        method: Int,
        school: Int
    ): PrayerTimesResponse
}