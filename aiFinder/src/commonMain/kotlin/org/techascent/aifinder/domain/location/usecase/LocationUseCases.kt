package org.techascent.aifinder.domain.location.usecase

import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.location.validated
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation

class ObserveLocationsUseCase(private val repository: LocationRepository) {
    operator fun invoke() = repository.observeLocations()
}

class CreateLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(
        request: CreateLocationRequest,
        existing: List<StorageLocation>,
    ): Result<StorageLocation> {
        val valid = runCatching { request.validated(existing) }.getOrElse { return Result.failure(it) }
        return repository.createLocation(valid)
    }
}
