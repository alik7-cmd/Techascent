package org.techascent.muslim.calendar.state

import kotlinx.datetime.LocalDate
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

/**
 * UI state for the Prayer Calendar screen.
 */
sealed interface CalendarUiState {
    data object Loading : CalendarUiState

    data class Success(
        /** All cached prayer data keyed by "DD-MM-YYYY" */
        val prayerDataMap: Map<String, PrayerTimeUiModel>,
        /** Currently displayed month */
        val displayedYear: Int,
        val displayedMonth: Int,
        /** Selected date key "DD-MM-YYYY" */
        val selectedDateKey: String?,
        /** The prayer data for the selected date */
        val selectedDayData: PrayerTimeUiModel?,
        /** Boundaries: the earliest & latest date that has data */
        val minDate: LocalDate?,
        val maxDate: LocalDate?,
    ) : CalendarUiState

    data class Error(val message: String) : CalendarUiState
}

