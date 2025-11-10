package org.techascent.shared.data.datasource

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimeMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.data.enum.School
import org.techascent.shared.network.ResultState

interface PrayerTimeDataSource {
    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        school: School,
        onMapData: (PrayerTimesResponse) -> PrayerTimeDto
    ): Flow<ResultState<PrayerTimeDto>>

    suspend fun getPrayerTimesByMonth(
        year: Int,
        month: Int,
        city: String,
        country: String,
        method: Int,
        school: Int,
        onMapData: (PrayerTimeMonthlyResponse) -> List<PrayerTimeDto>
    ): Flow<ResultState<List<PrayerTimeDto>>>
}