package org.techascent.shared.data.api

import org.techascent.shared.data.PrayerTimesResponse


interface PrayerApi {
    suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        method: Int
    ): PrayerTimesResponse
}