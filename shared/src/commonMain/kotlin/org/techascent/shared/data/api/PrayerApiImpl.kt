package org.techascent.shared.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.techascent.shared.data.PrayerTimesMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse

private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val SCHOOL = "school"
private const val CITY = "city"
private const val COUNTRY = "country"

class PrayerApiImpl(private val client: HttpClient) : PrayerApi {
    override suspend fun getPrayerTimes(
        date: String,
        latitude: Double,
        longitude: Double,
        school: Int
    ): PrayerTimesResponse {
        return client.get("https://api.aladhan.com/v1/timings/$date") {
            parameter(key = LATITUDE, value = latitude)
            parameter(key = LONGITUDE, value = longitude)
            parameter(key = SCHOOL, value = school)
        }.body()
    }

    override suspend fun getMonthlyPrayerTimes(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        school: Int
    ): PrayerTimesMonthlyResponse {
        return client.get("https://api.aladhan.com/v1/calendar/$year/$month") {
            parameter(key = LATITUDE, value = latitude)
            parameter(key = LONGITUDE, value = longitude)
            parameter(key = SCHOOL, value = school)
        }.body()
    }
}