package org.techascent.shared.data.repository.quran

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.techascent.shared.data.SurahDetailData
import org.techascent.shared.data.SurahDetailResponse
import org.techascent.shared.data.SurahInfo
import org.techascent.shared.data.datasource.quran.QuranDataSource
import org.techascent.shared.network.ResultState

class QuranRepositoryImpl(
    private val dataSource: QuranDataSource,
    private val dataStore: DataStore<Preferences>,
) : QuranRepository {

    companion object {
        private val SURAH_LIST_KEY = stringPreferencesKey("quran_surah_list_cache")
        private fun surahAudioKey(surahNumber: Int) = stringPreferencesKey("quran_surah_audio_$surahNumber")
        private fun surahTranslationKey(surahNumber: Int, edition: String) =
            stringPreferencesKey("quran_surah_translation_${surahNumber}_$edition")

        private val json = Json { ignoreUnknownKeys = true }
    }

    override fun getSurahList(): Flow<ResultState<List<SurahInfo>>> = flow {
        emit(ResultState.Loading)

        // Try cache first
        val cached = getCachedSurahList()
        if (cached != null) {
            emit(ResultState.Success(cached))
            return@flow
        }

        // Fetch from API
        dataSource.getSurahList().collect { result ->
            if (result is ResultState.Success) {
                saveSurahListToCache(result.data)
            }
            emit(result)
        }
    }

    override fun getSurahWithAudio(surahNumber: Int): Flow<ResultState<SurahDetailResponse>> = flow {
        emit(ResultState.Loading)

        // Try cache first
        val cached = getCachedSurahAudio(surahNumber)
        if (cached != null) {
            emit(ResultState.Success(
                SurahDetailResponse(code = 200, status = "OK", data = cached)
            ))
            return@flow
        }

        // Fetch from API
        dataSource.getSurahWithAudio(surahNumber).collect { result ->
            if (result is ResultState.Success) {
                saveSurahAudioToCache(surahNumber, result.data.data)
            }
            emit(result)
        }
    }

    override fun getSurahTranslation(
        surahNumber: Int,
        edition: String
    ): Flow<ResultState<SurahDetailResponse>> = flow {
        emit(ResultState.Loading)

        // Try cache first
        val cached = getCachedSurahTranslation(surahNumber, edition)
        if (cached != null) {
            emit(ResultState.Success(
                SurahDetailResponse(code = 200, status = "OK", data = cached)
            ))
            return@flow
        }

        // Fetch from API
        dataSource.getSurahTranslation(surahNumber, edition).collect { result ->
            if (result is ResultState.Success) {
                saveSurahTranslationToCache(surahNumber, edition, result.data.data)
            }
            emit(result)
        }
    }

    // ---- Cache Read ----

    private suspend fun getCachedSurahList(): List<SurahInfo>? {
        return try {
            val prefs = dataStore.data.first()
            val jsonString = prefs[SURAH_LIST_KEY] ?: return null
            json.decodeFromString<List<SurahInfo>>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCachedSurahAudio(surahNumber: Int): SurahDetailData? {
        return try {
            val prefs = dataStore.data.first()
            val jsonString = prefs[surahAudioKey(surahNumber)] ?: return null
            json.decodeFromString<SurahDetailData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCachedSurahTranslation(surahNumber: Int, edition: String): SurahDetailData? {
        return try {
            val prefs = dataStore.data.first()
            val jsonString = prefs[surahTranslationKey(surahNumber, edition)] ?: return null
            json.decodeFromString<SurahDetailData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    // ---- Cache Write ----

    private suspend fun saveSurahListToCache(surahs: List<SurahInfo>) {
        try {
            val jsonString = json.encodeToString(surahs)
            dataStore.edit { prefs ->
                prefs[SURAH_LIST_KEY] = jsonString
            }
        } catch (e: Exception) {
            // Cache write failure is non-fatal
        }
    }

    private suspend fun saveSurahAudioToCache(surahNumber: Int, data: SurahDetailData) {
        try {
            val jsonString = json.encodeToString(data)
            dataStore.edit { prefs ->
                prefs[surahAudioKey(surahNumber)] = jsonString
            }
        } catch (e: Exception) {
            // Cache write failure is non-fatal
        }
    }

    private suspend fun saveSurahTranslationToCache(surahNumber: Int, edition: String, data: SurahDetailData) {
        try {
            val jsonString = json.encodeToString(data)
            dataStore.edit { prefs ->
                prefs[surahTranslationKey(surahNumber, edition)] = jsonString
            }
        } catch (e: Exception) {
            // Cache write failure is non-fatal
        }
    }
}
