package org.techascent.muslim.quran

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.quran.audio.QuranAudioPlayer
import org.techascent.muslim.quran.audio.createQuranAudioPlayer
import org.techascent.muslim.quran.state.AyahUiModel
import org.techascent.muslim.quran.state.SurahDetailUiState
import org.techascent.muslim.quran.state.SurahListUiState
import org.techascent.shared.data.repository.quran.QuranRepository
import org.techascent.shared.network.ResultState

class QuranViewModel(
    private val repository: QuranRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    companion object {
        private val LAST_SURAH_KEY = intPreferencesKey(DataStoreKey.LAST_SURAH_NUMBER)
        private val LAST_AYAH_KEY = intPreferencesKey(DataStoreKey.LAST_AYAH_INDEX)
    }

    private val _surahListState = MutableStateFlow<SurahListUiState>(SurahListUiState.Loading)
    val surahListState: StateFlow<SurahListUiState> = _surahListState.asStateFlow()

    private val _surahDetailState = MutableStateFlow(SurahDetailUiState())
    val surahDetailState: StateFlow<SurahDetailUiState> = _surahDetailState.asStateFlow()

    val audioPlayer = createQuranAudioPlayer()

    init {
        loadSurahList()
        setupAutoPlayNext()
    }

    private fun setupAutoPlayNext() {
        audioPlayer.setOnCompletionListener {
            val currentState = _surahDetailState.value
            val currentAyah = audioPlayer.currentAyahNumber.value
            val currentIndex = currentState.ayahs.indexOfFirst { it.numberInSurah == currentAyah }

            if (currentIndex >= 0 && currentIndex < currentState.ayahs.size - 1) {
                val nextAyah = currentState.ayahs[currentIndex + 1]
                nextAyah.audioUrl?.let { url ->
                    audioPlayer.play(url, nextAyah.numberInSurah)
                    _surahDetailState.update { state ->
                        state.copy(
                            currentlyPlayingAyah = nextAyah.numberInSurah,
                            ayahs = state.ayahs.map {
                                it.copy(isPlaying = it.numberInSurah == nextAyah.numberInSurah)
                            }
                        )
                    }
                }
            } else {
                // Surah finished
                _surahDetailState.update { state ->
                    state.copy(
                        currentlyPlayingAyah = -1,
                        ayahs = state.ayahs.map { it.copy(isPlaying = false) }
                    )
                }
            }
        }
    }

    fun loadSurahList() {
        viewModelScope.launch {
            val lastSurah = dataStore.data.first()[LAST_SURAH_KEY] ?: -1
            repository.getSurahList().collect { result ->
                _surahListState.value = when (result) {
                    is ResultState.Loading -> SurahListUiState.Loading
                    is ResultState.Success -> SurahListUiState.Success(
                        surahs = result.data,
                        lastSurahNumber = lastSurah
                    )
                    is ResultState.Error -> SurahListUiState.Error(
                        message = result.message ?: "Failed to load surahs"
                    )
                }
            }
        }
    }

    fun loadSurahDetail(surahNumber: Int) {
        viewModelScope.launch {
            _surahDetailState.value = SurahDetailUiState(isLoading = true)

            val lastAyah = dataStore.data.first()[LAST_AYAH_KEY] ?: 0
            val lastSurah = dataStore.data.first()[LAST_SURAH_KEY] ?: -1
            val savedAyahIndex = if (lastSurah == surahNumber) lastAyah else 0

            // Fetch audio edition and translation in parallel
            var arabicAyahs: List<org.techascent.shared.data.AyahData> = emptyList()
            var translationAyahs: List<org.techascent.shared.data.AyahData> = emptyList()
            var surahName = ""
            var surahEnglishName = ""
            var surahTranslation = ""
            var hasError = false
            var errorMessage = ""

            // Fetch Arabic + Audio
            repository.getSurahWithAudio(surahNumber).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        arabicAyahs = result.data.data.ayahs
                        surahName = result.data.data.name
                        surahEnglishName = result.data.data.englishName
                        surahTranslation = result.data.data.englishNameTranslation
                    }
                    is ResultState.Error -> {
                        hasError = true
                        errorMessage = result.message ?: "Failed to load surah"
                    }
                    is ResultState.Loading -> { /* handled by initial state */ }
                }
            }

            if (hasError) {
                _surahDetailState.value = SurahDetailUiState(
                    isLoading = false,
                    error = errorMessage
                )
                return@launch
            }

            // Fetch Translation
            repository.getSurahTranslation(surahNumber).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        translationAyahs = result.data.data.ayahs
                    }
                    is ResultState.Error -> {
                        // Translation failure is non-fatal, proceed without it
                    }
                    is ResultState.Loading -> { /* handled */ }
                }
            }

            // Merge Arabic + Translation
            val ayahUiModels = arabicAyahs.map { arabicAyah ->
                val translation = translationAyahs.find {
                    it.numberInSurah == arabicAyah.numberInSurah
                }
                AyahUiModel(
                    numberInSurah = arabicAyah.numberInSurah,
                    arabicText = arabicAyah.text,
                    translationText = translation?.text ?: "",
                    audioUrl = arabicAyah.audio,
                )
            }

            _surahDetailState.value = SurahDetailUiState(
                isLoading = false,
                surahName = surahName,
                surahEnglishName = surahEnglishName,
                surahTranslation = surahTranslation,
                ayahs = ayahUiModels,
                lastAyahIndex = savedAyahIndex,
            )
        }
    }

    fun onPlayPauseAyah(ayah: AyahUiModel) {
        val currentPlaying = audioPlayer.currentAyahNumber.value
        if (currentPlaying == ayah.numberInSurah && audioPlayer.isPlaying.value) {
            // Pause
            audioPlayer.pause()
            _surahDetailState.update { state ->
                state.copy(
                    currentlyPlayingAyah = ayah.numberInSurah,
                    ayahs = state.ayahs.map {
                        it.copy(isPlaying = false)
                    }
                )
            }
        } else {
            // Play
            ayah.audioUrl?.let { url ->
                audioPlayer.play(url, ayah.numberInSurah)
                _surahDetailState.update { state ->
                    state.copy(
                        currentlyPlayingAyah = ayah.numberInSurah,
                        ayahs = state.ayahs.map {
                            it.copy(isPlaying = it.numberInSurah == ayah.numberInSurah)
                        }
                    )
                }
            }
        }
    }

    fun saveCurrentPosition(surahNumber: Int, ayahIndex: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[LAST_SURAH_KEY] = surahNumber
                preferences[LAST_AYAH_KEY] = ayahIndex
            }
        }
    }

    fun stopAudioAndSavePosition(surahNumber: Int) {
        val currentAyah = audioPlayer.currentAyahNumber.value
        val ayahIndex = if (currentAyah > 0) {
            _surahDetailState.value.ayahs.indexOfFirst { it.numberInSurah == currentAyah }
                .coerceAtLeast(0)
        } else {
            0
        }
        audioPlayer.stop()
        _surahDetailState.update { state ->
            state.copy(
                currentlyPlayingAyah = -1,
                ayahs = state.ayahs.map { it.copy(isPlaying = false) }
            )
        }
        saveCurrentPosition(surahNumber, ayahIndex)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

