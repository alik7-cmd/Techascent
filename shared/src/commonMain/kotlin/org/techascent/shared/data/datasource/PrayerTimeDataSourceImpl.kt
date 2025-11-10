package org.techascent.shared.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.techascent.shared.data.PrayerTimeMonthlyResponse
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.api.PrayerApi
import org.techascent.shared.data.cache.CacheService
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.enum.toCode
import org.techascent.shared.data.room.PrayerTimesDao
import org.techascent.shared.data.room.PrayerTimesEntity
import org.techascent.shared.network.ResultState
import org.techascent.shared.network.baseRemoteCall

class PrayerTimeDataSourceImpl(
    private val api: PrayerApi,
    private val cacheService: CacheService<String, PrayerTimesResponse>,
    private val dao: PrayerTimesDao
) : PrayerTimeDataSource {
    override fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String,
        school: School,
        onMapData: (PrayerTimesResponse) -> PrayerTimeDto
    ): Flow<ResultState<PrayerTimeDto>> = flow {
        val cacheKey = "$latitude-$longitude-$date-${school.toCode()}"
        val cached = cacheService.get(cacheKey)
        if (cached != null) {
            emit(ResultState.Success(onMapData(cached)))
        } else {
            emitAll(
                baseRemoteCall(
                    onCallRemoteApi = {
                        api.getPrayerTimes(
                            date = date,
                            latitude = latitude,
                            longitude = longitude,
                            school = school.toCode()
                        )
                    },
                    onMapData = { response ->
                        // Save in cache before mapping
                        cacheService.put(cacheKey, response)
                        onMapData(response)
                    }
                )
            )
        }
    }

    override suspend fun getPrayerTimesByMonth(
        year: Int,
        month: Int,
        city: String,
        country: String,
        method: Int,
        school: Int,
        onMapData: (PrayerTimeMonthlyResponse) -> List<PrayerTimeDto>
    ): Flow<ResultState<List<PrayerTimeDto>>> = flow {
        // Try to get from database first
        val cachedData = dao.getPrayerTimes()
        if (cachedData != null) {
            emit(
                ResultState.Success(
                    onMapData(cachedData.response)
                )
            )
        } else {
            // If not in database, fetch from API
            emitAll(
                baseRemoteCall(
                    onCallRemoteApi = {
                        api.getPrayerTimesByMonth(
                            year = year,
                            month = month,
                            city = city,
                            country = country,
                            method = method,
                            school = school
                        )
                    },
                    onMapData = { response ->
                        onMapData(response)
                    }
                ).onEach { result ->
                    if (result is ResultState.Success) {
                        // Store the response before it's mapped
                        val monthlyResponse = api.getPrayerTimesByMonth(
                            year, month, city, country, method, school
                        )
                        savePrayerTimesToDatabase(monthlyResponse)
                    }
                }
            )
        }
    }

    suspend fun savePrayerTimesToDatabase(response: PrayerTimeMonthlyResponse) {
        dao.insertPrayerTimes(
            PrayerTimesEntity(
                id = 1,
                response = response
            )
        )
    }


}