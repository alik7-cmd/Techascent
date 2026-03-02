package org.techascent.muslim.quran.state

data class AyahUiModel(
    val numberInSurah: Int,
    val arabicText: String,
    val translationText: String,
    val audioUrl: String?,
    val isPlaying: Boolean = false,
)

data class SurahDetailUiState(
    val isLoading: Boolean = true,
    val surahName: String = "",
    val surahEnglishName: String = "",
    val surahTranslation: String = "",
    val ayahs: List<AyahUiModel> = emptyList(),
    val currentlyPlayingAyah: Int = -1,
    val error: String? = null,
    val lastAyahIndex: Int = 0,
)

