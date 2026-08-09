package org.techascent.muslim.prayer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.utility.FeatureUsageRepository
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.formatForDisplay
import org.techascent.muslim.prayer.usecase.PrayerNotificationUseCase
import org.techascent.muslim.prayer.usecase.PrayerTimeViewUseCase
import org.techascent.muslim.refreshHomeWidgets
import org.techascent.shared.network.ResultState
import kotlin.time.ExperimentalTime

class PrayerTimeViewModel(
    private val prayerTimeUseCase: PrayerTimeViewUseCase,
    private val prayerNotificationUseCase: PrayerNotificationUseCase,
    private val dataStore: DataStore<Preferences>,
    private val featureUsageRepository: FeatureUsageRepository,
) : ViewModel() {

    /** Raw prayer data — always stored in 24hr format (cache-friendly). */
    private val _rawState: MutableStateFlow<PrayerTimeUiState> =
        MutableStateFlow(PrayerTimeUiState.Loading)

    /** Observe the user's 24hr format preference reactively. */
    private val is24HourFormat: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[booleanPreferencesKey(DataStoreKey.IS_24_HOUR_FORMAT)] ?: false }

    /**
     * Public UI state: combines raw prayer data, the 24-hr format preference, and the
     * top-features usage data into a single emission so the UI always stays consistent.
     *
     * - Time format toggle → re-formats times instantly without a network call.
     * - Feature usage update → Quick Access section updates reactively on the same state.
     *
     * Fix 6: distinctUntilChanged on _rawState and is24HourFormat so that formatForDisplay
     * is NOT re-run when only topFeatures changes.
     */
    val uiState = combine(
        _rawState,                              // StateFlow — already distinctUntilChanged by design
        is24HourFormat.distinctUntilChanged(),  // plain Flow — skip formatForDisplay when unchanged
        featureUsageRepository.getTopFeatures(),
    ) { state, format, features ->
        when (state) {
            is PrayerTimeUiState.Success -> PrayerTimeUiState.Success(
                data = state.data.formatForDisplay(format),
                topFeatures = features,
            )
            is PrayerTimeUiState.SuccessWithWarning -> PrayerTimeUiState.SuccessWithWarning(
                data = state.data.formatForDisplay(format),
                cityName = state.cityName,
                topFeatures = features,
            )
            else -> state
        }
    }.onStart {
        getMonthlyPrayerTimes()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrayerTimeUiState.Loading,
    )

    private val _event: Channel<PrayerTimeEvent> = Channel()
    val event: Flow<PrayerTimeEvent> = _event.receiveAsFlow()

    /**
     * Timestamp of the last successful fetch initiation.
     * Prevents GPS + geocoder from re-running when the user briefly navigates
     * away and back — but ONLY when we already have a clean [PrayerTimeUiState.Success].
     *
     * [PrayerTimeUiState.SuccessWithWarning] is intentionally excluded from the guard
     * so that the app retries immediately when the user returns to the screen after
     * enabling GPS, clearing the location banner as soon as a fix is obtained.
     */
    private var lastFetchTimestamp: Long = 0L

    @OptIn(ExperimentalTime::class)
    internal fun getMonthlyPrayerTimes() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastFetchTimestamp < 60_000L && _rawState.value !is PrayerTimeUiState.Error) return
        lastFetchTimestamp = now
        viewModelScope.launch {
            prayerTimeUseCase.getMonthlyPrayerTimes().collect {
                when (it) {
                    is ResultState.Success -> {
                        schedulePrayerNotifications(it.data)
                        _rawState.emit(
                            value = PrayerTimeUiState.Success(data = it.data)
                        )
                        // Refresh home-screen widget with latest prayer data
                        refreshHomeWidgets()
                    }

                    is ResultState.Warning -> {
                        schedulePrayerNotifications(it.data)
                        _rawState.emit(
                            value = PrayerTimeUiState.SuccessWithWarning(
                                data = it.data,
                                cityName = it.message
                            )
                        )
                        refreshHomeWidgets()
                    }

                    is ResultState.Error -> _rawState.emit(
                        value = PrayerTimeUiState.Error(
                            message = it.message ?: ""
                        )
                    )

                    is ResultState.Loading -> _rawState.emit(value = PrayerTimeUiState.Loading)
                }
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

            val currentState = _rawState.value
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

                // Update raw state (combine will re-apply formatting)
                _rawState.emit(PrayerTimeUiState.Success(data = updatedData))

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
