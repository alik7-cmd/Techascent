package org.techascent.muslim.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.techascent.muslim.calendar.state.CalendarUiState
import org.techascent.muslim.common.getCurrentDateFormatted
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.enum.PrayerCalculationMethod
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

class CalendarViewModel(
    val repository: PrayerTimesRepository,
    val locationService: LocationService
) : ViewModel() {

    private val _uiState: MutableStateFlow<CalendarUiState> =
        MutableStateFlow(CalendarUiState.Loading)
    val uiState = _uiState.onStart {
        fetchPrayer(date = getCurrentDateFormatted())

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState.Loading
    )

    internal fun fetchPrayer(date: String) = viewModelScope.launch {
        val location = locationService.getCurrentLocation()
        location?.let { location ->
            repository.getPrayerTimes(
                latitude = location.latitude,
                longitude = location.longitude,
                date = date,
                method = PrayerCalculationMethod.MWL
            ).collect {
                when (it) {
                    is ResultState.Success -> _uiState.emit(
                        value =
                            CalendarUiState.Success(
                                data = it.data.toUiModel()
                            )
                    )

                    is ResultState.Error -> _uiState.emit(
                        value = CalendarUiState.Error(
                            message = it.message ?: ""
                        )
                    )

                    is ResultState.Loading -> _uiState.emit(value = CalendarUiState.Loading)
                }
            }
        }

    }
}