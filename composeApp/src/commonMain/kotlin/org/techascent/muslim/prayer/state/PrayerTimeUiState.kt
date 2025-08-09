package org.techascent.muslim.prayer.state

import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

sealed interface PrayerTimeUiState {
    data object Loading : PrayerTimeUiState
    data class Success(val data: PrayerTimeUiModel) : PrayerTimeUiState
    data class Error(val message: String) : PrayerTimeUiState
}