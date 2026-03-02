package org.techascent.shared.data.datasource.quran

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.SurahDetailResponse
import org.techascent.shared.data.SurahInfo
import org.techascent.shared.network.ResultState

interface QuranDataSource {
    fun getSurahList(): Flow<ResultState<List<SurahInfo>>>

    fun getSurahWithAudio(surahNumber: Int): Flow<ResultState<SurahDetailResponse>>

    fun getSurahTranslation(surahNumber: Int, edition: String = "en.asad"): Flow<ResultState<SurahDetailResponse>>
}

