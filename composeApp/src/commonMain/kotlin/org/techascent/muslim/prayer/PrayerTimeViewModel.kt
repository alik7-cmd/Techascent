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
import kotlinx.datetime.Clock.System
import org.techascent.muslim.common.toReadableDate
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.usecase.PrayerTimeViewUseCase
import org.techascent.shared.network.ResultState
import kotlin.time.ExperimentalTime

class PrayerTimeViewModel(
    private val prayerTimeUseCase: PrayerTimeViewUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<PrayerTimeUiState> =
        MutableStateFlow(PrayerTimeUiState.Loading)
    val uiState = _uiState.onStart {
        getMonthlyPrayerTimes()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrayerTimeUiState.Loading
    )

    private val _event: Channel<PrayerTimeEvent> = Channel()
    val event: Flow<PrayerTimeEvent> = _event.receiveAsFlow()

    @OptIn(ExperimentalTime::class)
    internal fun getMonthlyPrayerTimes() = viewModelScope.launch {
        prayerTimeUseCase.getMonthlyPrayerTimes().collect {
            when (it) {
                is ResultState.Success -> {
                    val data = it.data.find { uiModel ->
                        uiModel.currentDateTime == System.now().toEpochMilliseconds()
                            .toReadableDate()

                    }
                    data?.let { uiModel ->
                        _uiState.emit(
                            value = PrayerTimeUiState.Success(data = uiModel)
                        )
                    }
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


    fun onHandleEvent(event: PrayerTimeEvent) = viewModelScope.launch {
        when (event) {
            is PrayerTimeEvent.OpenExternalLink -> _event.send(element = event)
        }
    }
}
