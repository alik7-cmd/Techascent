package org.techascent.muslim.quran.state

import org.techascent.shared.data.SurahInfo

sealed interface SurahListUiState {
    data object Loading : SurahListUiState
    data class Success(val surahs: List<SurahInfo>, val lastSurahNumber: Int = -1) : SurahListUiState
    data class Error(val message: String) : SurahListUiState
}

