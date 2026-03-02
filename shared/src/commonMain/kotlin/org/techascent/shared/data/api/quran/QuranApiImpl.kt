package org.techascent.shared.data.api.quran

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.techascent.shared.data.SurahDetailResponse
import org.techascent.shared.data.SurahListResponse

class QuranApiImpl(private val client: HttpClient) : QuranApi {

    companion object {
        private const val BASE_URL = "https://api.alquran.cloud/v1"
    }

    override suspend fun getSurahList(): SurahListResponse {
        return client.get("$BASE_URL/surah").body()
    }

    override suspend fun getSurahWithAudio(surahNumber: Int): SurahDetailResponse {
        return client.get("$BASE_URL/surah/$surahNumber/ar.alafasy").body()
    }

    override suspend fun getSurahTranslation(surahNumber: Int, edition: String): SurahDetailResponse {
        return client.get("$BASE_URL/surah/$surahNumber/$edition").body()
    }
}

