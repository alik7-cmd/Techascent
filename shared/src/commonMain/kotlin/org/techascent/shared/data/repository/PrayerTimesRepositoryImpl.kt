package org.techascent.shared.data.repository

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.datasource.PrayerTimeDataSource
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.network.ResultState
import org.techascent.shared.data.mapper.toDto

class PrayerTimesRepositoryImpl(
    private val dataSource: PrayerTimeDataSource
) : PrayerTimesRepository {
    override fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        method: PrayerCalculationMethod,
    ): Flow<ResultState<PrayerTimeDto>> {
        return dataSource.getPrayerTimes(latitude, longitude, date, method, onMapData = {response ->
            response.toDto()
        })
    }
}

