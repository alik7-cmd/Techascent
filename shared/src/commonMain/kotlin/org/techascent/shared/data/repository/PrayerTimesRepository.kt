package org.techascent.shared.data.repository

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.network.ResultState

interface PrayerTimesRepository {
    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        method: PrayerCalculationMethod,
    ): Flow<ResultState<PrayerTimeDto>>
}