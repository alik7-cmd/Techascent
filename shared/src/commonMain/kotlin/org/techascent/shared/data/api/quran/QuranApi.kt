package org.techascent.shared.data.api.quran

import org.techascent.shared.data.SurahDetailResponse
import org.techascent.shared.data.SurahListResponse

interface QuranApi {
    suspend fun getSurahList(): SurahListResponse

    suspend fun getSurahWithAudio(surahNumber: Int): SurahDetailResponse

    suspend fun getSurahTranslation(surahNumber: Int, edition: String = "en.asad"): SurahDetailResponse
}

