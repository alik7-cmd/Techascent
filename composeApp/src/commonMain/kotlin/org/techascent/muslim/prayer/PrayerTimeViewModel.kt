package org.techascent.muslim.prayer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.techascent.muslim.common.getCurrentDateFormatted
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

class PrayerTimeViewModel(
    val repository: PrayerTimesRepository,
    val locationService: LocationService,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val PRAYER_TIMES_CACHE_KEY = stringPreferencesKey("prayer_times_cache")
    }

    private val _uiState: MutableStateFlow<PrayerTimeUiState> =
        MutableStateFlow(PrayerTimeUiState.Loading)
    val uiState = _uiState.onStart {
        getPrayerTimes()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrayerTimeUiState.Loading
    )

    private val _event: Channel<PrayerTimeEvent> = Channel()
    val event: Flow<PrayerTimeEvent> = _event.receiveAsFlow()

    internal fun getPrayerTimes() = viewModelScope.launch {
        val cachedData = getCachedPrayerTimes()
        if (cachedData != null) {
            _uiState.emit(value = PrayerTimeUiState.Success(data = cachedData))
            return@launch
        }

        val location = locationService.getCurrentLocation()
        location?.let { location ->
            repository.getPrayerTimes(
                latitude = location.latitude,
                longitude = location.longitude,
                date = getCurrentDateFormatted(),
                school = School.HANAFI
            ).collect {
                when (it) {
                    is ResultState.Success -> {
                        val uiModel = it.data.toUiModel()
                        savePrayerTimesToCache(uiModel)
                        _uiState.emit(
                            value = PrayerTimeUiState.Success(data = uiModel)
                        )
                    }

                    is ResultState.Error -> _uiState.emit(
                        value = PrayerTimeUiState.Error(
                            message = it.message ?: ""
                        )
                    )

                    is ResultState.Loading -> _uiState.emit(value = PrayerTimeUiState.Loading)
                }
            }
        }
    }

    private suspend fun getCachedPrayerTimes(): PrayerTimeUiModel? {
        return try {
            val jsonString = dataStore.data.first()[PRAYER_TIMES_CACHE_KEY]
            jsonString?.let {
                Json.decodeFromString<PrayerTimeUiModel>(it)
            }

        }catch (e: Exception){
            null
        }
    }

    private suspend fun savePrayerTimesToCache(uiModel: PrayerTimeUiModel) {
        try {
            val jsonString = Json.encodeToString(uiModel)
            dataStore.edit { preferences ->
                preferences[PRAYER_TIMES_CACHE_KEY] = jsonString
            }
        } catch (e: Exception) {
            print("error ${e.message}")
        }
    }


    fun onHandleEvent(event: PrayerTimeEvent) = viewModelScope.launch {
        when (event) {
            is PrayerTimeEvent.OpenExternalLink -> _event.send(element = event)
        }
    }
}
