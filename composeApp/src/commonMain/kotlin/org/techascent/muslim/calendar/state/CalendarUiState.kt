package org.techascent.muslim.calendar.state

import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Success(val data: PrayerTimeUiModel) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}