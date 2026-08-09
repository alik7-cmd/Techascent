package org.techascent.muslim.calendar

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.techascent.muslim.calendar.state.CalendarUiState
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.prayer.uimodel.formatForDisplay
import org.techascent.muslim.prayer.usecase.GetCachedPrayerDataUseCase
import org.techascent.shared.data.common.currentDate
import org.techascent.shared.data.common.parseDateKey
import org.techascent.shared.data.common.toDayMonthYearString

class CalendarViewModel(
    private val getCachedPrayerData: GetCachedPrayerDataUseCase,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCachedData()
    }

    private fun loadCachedData() = viewModelScope.launch {
        try {
            val prefs = dataStore.data.first()
            val is24Hour = prefs[booleanPreferencesKey(DataStoreKey.IS_24_HOUR_FORMAT)] ?: false
            val allData = getCachedPrayerData()
                .mapValues { (_, model) -> model.formatForDisplay(is24Hour) }
            val today = currentDate
            val todayKey = today.toDayMonthYearString()

            val dates = allData.keys.mapNotNull { parseDateKey(it) }.sorted()

            _uiState.value = CalendarUiState.Success(
                prayerDataMap = allData,
                displayedYear = today.year,
                displayedMonth = today.monthNumber,
                selectedDateKey = todayKey,
                selectedDayData = allData[todayKey],
                minDate = dates.firstOrNull(),
                maxDate = dates.lastOrNull(),
            )
        } catch (e: Exception) {
            _uiState.value = CalendarUiState.Error(e.message ?: "")
        }
    }

    fun onSelectDate(dateKey: String) {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            _uiState.value = current.copy(
                selectedDateKey = dateKey,
                selectedDayData = current.prayerDataMap[dateKey],
            )
        }
    }

    fun onPreviousMonth() {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            val prev = LocalDate(current.displayedYear, current.displayedMonth, 1)
                .minus(1, DateTimeUnit.MONTH)
            _uiState.value = current.copy(
                displayedYear = prev.year,
                displayedMonth = prev.monthNumber,
            )
        }
    }

    fun onNextMonth() {
        val current = _uiState.value
        if (current is CalendarUiState.Success) {
            val next = LocalDate(current.displayedYear, current.displayedMonth, 1)
                .plus(1, DateTimeUnit.MONTH)
            _uiState.value = current.copy(
                displayedYear = next.year,
                displayedMonth = next.monthNumber,
            )
        }
    }
}
