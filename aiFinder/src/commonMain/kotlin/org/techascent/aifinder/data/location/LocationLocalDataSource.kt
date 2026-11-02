package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow

/** Device cache of a user's locations; the source the UI reads from. */
interface LocationLocalDataSource {
    fun observe(userId: String): Flow<List<Pair<String, LocationDto>>>
    suspend fun isEmpty(userId: String): Boolean
    suspend fun replaceAll(userId: String, nodes: List<Pair<String, LocationDto>>)
    suspend fun upsert(userId: String, id: String, location: LocationDto)
}
