package org.techascent.shared.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.techascent.shared.data.PrayerTimesResponse

private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val METHOD = "method"

class PrayerApiImpl(private val client: HttpClient) : PrayerApi {
    override suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        method: Int
    ): PrayerTimesResponse {
        return client.get("https://api.aladhan.com/v1/timings/$date") {
            parameter(key = LATITUDE, value = latitude)
            parameter(key = LONGITUDE, value = longitude)
            parameter(key = METHOD, value = method)
        }.body()
    }
}