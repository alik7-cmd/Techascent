package org.techascent.shared.data.repository

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimeMonthlyResponse
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
        school: School,
    ): Flow<ResultState<PrayerTimeDto>> {
        return dataSource.getPrayerTimes(
            latitude,
            longitude,
            date,
            school,
            onMapData = { response ->
                response.data.toDto()
            })
    }

    override suspend fun getPrayerTimesByMonth(
        year: Int,
        month: Int,
        city: String,
        country: String,
        method: Int,
        school: Int,
        onMapData: (PrayerTimeMonthlyResponse) -> List<PrayerTimeDto>
    ): Flow<ResultState<List<PrayerTimeDto>>> {
        return dataSource.getPrayerTimesByMonth(
            year = year,
            month = month,
            city = city,
            country = country,
            method = method,
            school = school, onMapData = { response ->
                response.data?.let {
                    it.map { data ->
                        data.toDto()
                    }
                } ?: emptyList()
            })
        return dataSource.getPrayerTimes(latitude, longitude, date, school, onMapData = {response ->
            response.toDto()
        })
    }
}

