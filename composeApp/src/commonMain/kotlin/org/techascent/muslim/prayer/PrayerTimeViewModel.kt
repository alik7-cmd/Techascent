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
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.usecase.PrayerNotificationUseCase
import org.techascent.muslim.prayer.usecase.PrayerTimeViewUseCase
import org.techascent.shared.network.ResultState
import kotlin.time.ExperimentalTime

class PrayerTimeViewModel(
    private val prayerTimeUseCase: PrayerTimeViewUseCase,
    private val prayerNotificationUseCase: PrayerNotificationUseCase
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
                    schedulePrayerNotifications(it.data)
                    _uiState.emit(
                        value = PrayerTimeUiState.Success(data = it.data)
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

    private fun schedulePrayerNotifications(uiModel: PrayerTimeUiModel) = viewModelScope.launch {
        prayerNotificationUseCase.schedulePrayerNotifications(uiModel.intervals)
    }

    fun onUpdateNotification(shouldSave: Boolean, prayerName: PrayerNameEnum) =
        viewModelScope.launch {
            if (shouldSave) {
                prayerNotificationUseCase.addPrayerToNotify(prayerName)
            } else {
                prayerNotificationUseCase.removePrayerFromNotify(prayerName)
            }

            val currentState = _uiState.value
            if (currentState is PrayerTimeUiState.Success) {
                val currentData = currentState.data
                // Update the shouldNotify flag in the UI model
                val updatedIntervals = currentData.intervals.map { interval ->
                    if (interval.name == prayerName) {
                        interval.copy(shouldNotify = shouldSave)
                    } else {
                        interval
                    }
                }
                val updatedData = currentData.copy(intervals = updatedIntervals)

                // Update UI state first
                _uiState.emit(PrayerTimeUiState.Success(data = updatedData))

                // Then reschedule notifications with updated intervals
                prayerNotificationUseCase.schedulePrayerNotifications(updatedIntervals)
            }
        }

    fun onHandleEvent(event: PrayerTimeEvent) = viewModelScope.launch {
        when (event) {
            is PrayerTimeEvent.OpenExternalLink -> _event.send(element = event)
        }
    }

    /**
     * Schedules 5 test azan notifications at 1-minute intervals.
     * Works even after the app is killed. Call this and then kill the app to test.
     */
    fun startRepeatingTestAzan() = viewModelScope.launch {
        prayerNotificationUseCase.startRepeatingTestNotification()
    }

    /**
     * Cancels all repeating test notifications.
     */
    fun stopRepeatingTestAzan() = viewModelScope.launch {
        prayerNotificationUseCase.stopRepeatingTestNotification()
    }
}

