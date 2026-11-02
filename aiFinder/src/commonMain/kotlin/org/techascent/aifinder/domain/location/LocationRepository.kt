package org.techascent.aifinder.domain.location

import kotlinx.coroutines.flow.Flow
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation

interface LocationRepository {
    /** Emits the signed-in user's locations and keeps emitting as they change remotely. */
    fun observeLocations(): Flow<List<StorageLocation>>

    suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation>
}
