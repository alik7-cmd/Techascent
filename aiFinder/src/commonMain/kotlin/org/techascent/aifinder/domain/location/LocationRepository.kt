package org.techascent.aifinder.domain.location

import kotlinx.coroutines.flow.Flow
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation

interface LocationRepository {
    /** Emits the signed-in user's cached locations, including local writes and explicit refreshes. */
    fun observeLocations(): Flow<List<StorageLocation>>

    suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation>
    suspend fun refresh(): Result<Unit> = Result.failure(LocationError.Unavailable)
}
