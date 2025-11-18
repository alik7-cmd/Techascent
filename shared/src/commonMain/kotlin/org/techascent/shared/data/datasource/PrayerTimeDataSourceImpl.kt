package org.techascent.shared.data.datasource

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimesMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.api.PrayerApi
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.enum.toCode
import org.techascent.shared.network.ResultState
import org.techascent.shared.network.baseRemoteCall

class PrayerTimeDataSourceImpl(
    private val api: PrayerApi,
) : PrayerTimeDataSource {
    override fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        school: School,
        onMapData: (PrayerTimesResponse) -> PrayerTimeDto
    ): Flow<ResultState<PrayerTimeDto>> {
        return baseRemoteCall(
            onCallRemoteApi = {
                api.getPrayerTimes(
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    school = school.toCode()
                )
            },
            onMapData = onMapData
        )
    }

    override fun getMonthlyPrayerTimes(
        year: Int,
        month: Int,
        city: String,
        country: String,
        school: Int,
        onMapData: (PrayerTimesMonthlyResponse) -> List<PrayerTimeDto>
    ): Flow<ResultState<List<PrayerTimeDto>>> {
        return baseRemoteCall(
            onCallRemoteApi = {
                api.getMonthlyPrayerTimes(
                    year = year,
                    month = month,
                    city = city,
                    country = country,
                    school = school
                )
            },
            onMapData = onMapData
        )
    }

}