package org.techascent.muslim.prayer.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.getPlaceName
import org.techascent.muslim.prayer.uimodel.AddressInfo
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

class PrayerTimeViewUseCase(
    private val repository: PrayerTimesRepository,
    private val locationService: LocationService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private fun getCacheKeyForMonthly(city: String, school: School): String =
            "monthly_prayer_times_${city}_${school.name}"
    }

    suspend fun getMonthlyPrayerTimes(
        year: Int,
        month: Int,
        city: String,
        country: String,
        school: School,
    ): Flow<ResultState<List<PrayerTimeUiModel>>> {
        val cacheKey = getCacheKeyForMonthly(city, school)
        val cachedData = getCachedMonthlyPrayerTimes(cacheKey)
        if (cachedData != null) {
            return flowOf(
                ResultState.Success(cachedData)
            )

        }
        return repository.getMonthlyPrayerTimes(
            year = year,
            month = month,
            city = city,
            country = country,
            school = school.ordinal
        ).map { resultState ->
            when (resultState) {
                is ResultState.Success -> {
                    val uiModels = resultState.data.map { it.toUiModel() }
                    saveMonthlyPrayerTimesToCache(cacheKey, uiModels)
                    ResultState.Success(uiModels)
                }

                is ResultState.Error -> resultState
                is ResultState.Loading -> resultState
            }
        }
    }

    suspend fun getCachedMonthlyPrayerTimes(cacheKey: String): List<PrayerTimeUiModel>? {
        return try {
            val jsonString = dataStore.data.first()[stringPreferencesKey(cacheKey)]
            jsonString?.let {
                Json.decodeFromString<List<PrayerTimeUiModel>>(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun saveMonthlyPrayerTimesToCache(
        cacheKey: String,
        uiModels: List<PrayerTimeUiModel>
    ) {
        try {
            val jsonString = Json.encodeToString(uiModels)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(cacheKey)] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getAddress(): AddressInfo? {
        val location = locationService.getCurrentLocation()
        return location?.let {
            getPlaceName(
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
    }
}
