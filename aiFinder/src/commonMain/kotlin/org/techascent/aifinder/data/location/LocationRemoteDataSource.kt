package org.techascent.aifinder.data.location

interface LocationRemoteDataSource {
    /** One-shot read of every `(id, dto)` the user owns. */
    suspend fun fetchAll(userId: String): List<Pair<String, LocationDto>>

    /** Writes the location and returns its generated id. */
    suspend fun create(userId: String, location: LocationDto): String
}

object RealtimeDatabasePaths {
    const val ROOT = "aifinder"
    fun locations(userId: String) = "$ROOT/users/$userId/locations"
    fun items(userId: String) = "$ROOT/users/$userId/items"
    fun signedIn(userId: String) = "$ROOT/users/$userId/signedIn"
    fun scanWallet(userId: String) = "$ROOT/users/$userId/scanWallet"
}
