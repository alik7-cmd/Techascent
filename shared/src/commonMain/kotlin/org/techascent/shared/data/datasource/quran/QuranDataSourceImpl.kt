package org.techascent.shared.data.datasource.quran

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.SurahDetailResponse
import org.techascent.shared.data.SurahInfo
import org.techascent.shared.data.api.quran.QuranApi
import org.techascent.shared.network.ResultState
import org.techascent.shared.network.baseRemoteCall

class QuranDataSourceImpl(
    private val api: QuranApi
) : QuranDataSource {

    override fun getSurahList(): Flow<ResultState<List<SurahInfo>>> {
        return baseRemoteCall(
            onCallRemoteApi = { api.getSurahList() },
            onMapData = { it.data }
        )
    }

    override fun getSurahWithAudio(surahNumber: Int): Flow<ResultState<SurahDetailResponse>> {
        return baseRemoteCall(
            onCallRemoteApi = { api.getSurahWithAudio(surahNumber) },
            onMapData = { it }
        )
    }

    override fun getSurahTranslation(surahNumber: Int, edition: String): Flow<ResultState<SurahDetailResponse>> {
        return baseRemoteCall(
            onCallRemoteApi = { api.getSurahTranslation(surahNumber, edition) },
            onMapData = { it }
        )
    }
}

