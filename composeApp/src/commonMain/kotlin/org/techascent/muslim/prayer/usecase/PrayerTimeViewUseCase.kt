package org.techascent.muslim.prayer.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.techascent.muslim.common.getCurrentDateFormatted
import org.techascent.muslim.common.getCurrentYearAndMonth
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.getPlaceName
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
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
        private fun getCacheKeyForMonthly(city: String, school: School, month: Int): String =
            "$DEFAULT${city}_${school.name}_${month}"

        private const val DEFAULT = DataStoreKey.MONTHLY_PRAYER_INITIAL
        private val NOTIFY_PRAYERS_KEY = stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)

    }

    suspend fun getMonthlyPrayerTimes(): Flow<ResultState<PrayerTimeUiModel>> {
        val location = locationService.getCurrentLocation()
        val date = getCurrentYearAndMonth()
        val code = dataStore.data.first()[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)]
            ?: School.HANAFI.code
        val currentDate = getCurrentDateFormatted()

        location?.let {
            val addressInfo = getPlaceName(it.latitude, it.longitude)
            val cacheKey = getCacheKeyForMonthly(
                city = addressInfo.district ?: DEFAULT,
                school = School.fromCode(code),
                month = date.month
            )
            val cachedData = getCachedMonthlyPrayerTimes(cacheKey)
            if (cachedData != null) {
                val prayerData = cachedData.find {
                    currentDate == it.currentDateTime
                }
                if (prayerData != null) {
                    val updatedData = updateCurrentPrayer(prayerData)
                    return flowOf(
                        ResultState.Success(updatedData)
                    )
                }
            }

            return repository.getMonthlyPrayerTimes(
                year = date.year,
                month = date.month,
                latitude = it.latitude,
                longitude = it.longitude,
                school = School.fromCode(code).code
            ).map { resultState ->
                when (resultState) {
                    is ResultState.Success -> {
                        val uiModels = resultState.data.map {
                            it.toUiModel(
                                school = School.fromCode(code)
                            )
                        }
                        saveMonthlyPrayerTimesToCache(cacheKey, uiModels)
                        val data = uiModels.find {
                            it.currentDateTime == currentDate
                        }
                        data?.let {
                            ResultState.Success(it)
                        } ?: ResultState.Error("")

                    }

                    is ResultState.Error -> resultState
                    is ResultState.Loading -> resultState
                }
            }
        }

        return flowOf(ResultState.Error("Location not found"))

    }

    private suspend fun getNotifyPrayersList(): List<PrayerNameEnum> {
        return try {
            val jsonString = dataStore.data.first()[NOTIFY_PRAYERS_KEY] ?: "[]"
            Json.decodeFromString<List<String>>(jsonString).mapNotNull {
                try {
                    PrayerNameEnum.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun updateCurrentPrayer(uiModel: PrayerTimeUiModel): PrayerTimeUiModel {
        val now = Clock.System.now()
        val currentPrayer = uiModel.intervals.find { interval ->
            interval.startTimeInstant != null &&
                    interval.endTimeInstant != null &&
                    now >= interval.startTimeInstant &&
                    now < interval.endTimeInstant
        }

        val currentList = getNotifyPrayersList().toMutableList()
        
        return uiModel.copy(
            currentPrayer = currentPrayer,
            intervals = uiModel.intervals.map {
                it.copy(
                    shouldNotify = currentList.contains(it.name)
                )
            }
        )
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
                val keysToRemove = preferences.asMap().keys
                    .filter { it.name.startsWith(DEFAULT) }

                keysToRemove.forEach { key ->
                    preferences.remove(key)
                }
                preferences[stringPreferencesKey(cacheKey)] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
