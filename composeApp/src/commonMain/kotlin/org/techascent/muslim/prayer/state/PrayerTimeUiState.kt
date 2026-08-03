package org.techascent.muslim.prayer.state

import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.utility.FeatureId

sealed interface PrayerTimeUiState {
    data object Loading : PrayerTimeUiState
    data class Success(
        val data: PrayerTimeUiModel,
        val topFeatures: List<FeatureId> = emptyList(),
    ) : PrayerTimeUiState
    data class SuccessWithWarning(
        val data: PrayerTimeUiModel,
        val cityName: String,
        val topFeatures: List<FeatureId> = emptyList(),
    ) : PrayerTimeUiState
    data class Error(val message: String) : PrayerTimeUiState
}