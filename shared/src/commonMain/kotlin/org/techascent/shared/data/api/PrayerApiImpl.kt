package org.techascent.shared.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.techascent.shared.data.PrayerTimesResponse

class PrayerApiImpl(private val client: HttpClient) : PrayerApi {
    override suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        method: Int
    ): PrayerTimesResponse {
        return client.get("/v1/timings/$date") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("method", method)
        }.body()
    }

}