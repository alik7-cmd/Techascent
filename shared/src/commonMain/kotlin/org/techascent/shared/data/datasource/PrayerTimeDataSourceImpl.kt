package org.techascent.shared.data.datasource

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.api.PrayerApi
import org.techascent.shared.data.cache.CacheService
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.data.enum.toCode
import org.techascent.shared.network.ResultState
import org.techascent.shared.network.baseRemoteCall

class PrayerTimeDataSourceImpl(
    private val api: PrayerApi,
    private val cacheService: CacheService<String, PrayerTimesResponse>
) : PrayerTimeDataSource {
    override fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        method: PrayerCalculationMethod,
        onMapData: (PrayerTimesResponse) -> PrayerTimeDto
    ): Flow<ResultState<PrayerTimeDto>> {
        return baseRemoteCall(
            onCallRemoteApi = {
                api.getPrayerTimes(
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    method = method.toCode()
                )
            },
            onMapData = onMapData
        )
    }
}