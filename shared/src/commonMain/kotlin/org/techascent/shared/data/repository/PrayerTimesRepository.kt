package org.techascent.shared.data.repository

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimesMonthlyResponse
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.data.enum.School
import org.techascent.shared.network.ResultState

interface PrayerTimesRepository {
    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        school: School,
    ): Flow<ResultState<PrayerTimeDto>>

    fun getMonthlyPrayerTimes(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        school: Int,
    ): Flow<ResultState<List<PrayerTimeDto>>>
}