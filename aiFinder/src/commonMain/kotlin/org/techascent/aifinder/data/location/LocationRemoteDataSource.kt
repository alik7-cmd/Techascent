package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow

interface LocationRemoteDataSource {
    /** Emits `(id, dto)` pairs for every location the user owns. */
    fun observe(userId: String): Flow<List<Pair<String, LocationDto>>>

    /** Writes the location and returns its generated id. */
    suspend fun create(userId: String, location: LocationDto): String
}

object RealtimeDatabasePaths {
    const val ROOT = "aifinder"
    fun locations(userId: String) = "$ROOT/users/$userId/locations"
}
