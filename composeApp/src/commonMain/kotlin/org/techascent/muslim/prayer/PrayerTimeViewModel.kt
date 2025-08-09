package org.techascent.muslim.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.techascent.muslim.common.getCurrentDateFormatted
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

class PrayerTimeViewModel(
    val repository: PrayerTimesRepository,
    val locationService: LocationService
) : ViewModel() {

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
        val location = locationService.getCurrentLocation()
        location?.let { location ->
            repository.getPrayerTimes(
                latitude = location.latitude,
                longitude = location.longitude,
                date = getCurrentDateFormatted(),
                method = PrayerCalculationMethod.MWL
            ).collect {
                when (it) {
                    is ResultState.Success -> _uiState.emit(
                        value =
                            PrayerTimeUiState.Success(
                                data = it.data.toUiModel()
                            )
                    )

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

    fun onHandleEvent(event: PrayerTimeEvent) = viewModelScope.launch {
        when (event) {
            is PrayerTimeEvent.OpenExternalLink -> _event.send(element = event)
        }
    }
}