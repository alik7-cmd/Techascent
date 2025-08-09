package org.techascent.shared.data.datasource

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.network.ResultState

interface PrayerTimeDataSource {
    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        method: PrayerCalculationMethod,
        onMapData: (PrayerTimesResponse) -> PrayerTimeDto
    ): Flow<ResultState<PrayerTimeDto>>
}