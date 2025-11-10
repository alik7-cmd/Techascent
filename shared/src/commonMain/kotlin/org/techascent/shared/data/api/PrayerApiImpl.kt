package org.techascent.shared.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.techascent.shared.data.PrayerTimeMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse

private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val SCHOOL = "school"
private const val METHOD = "method"
private const val CITY = "city"
private const val COUNTRY = "country"

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

    override suspend fun getPrayerTimesByMonth(
        year: Int,
        month: Int,
        city: String,
        country: String,
        method: Int,
        school: Int
    ): PrayerTimeMonthlyResponse {
        return client.get("https://api.aladhan.com/v1/timings/$year/$month") {
            parameter(key = CITY, value = city)
            parameter(key = COUNTRY, value = country)
            parameter(key = METHOD, value = method)
            parameter(key = SCHOOL, value = method)
        }.body()
    }
}