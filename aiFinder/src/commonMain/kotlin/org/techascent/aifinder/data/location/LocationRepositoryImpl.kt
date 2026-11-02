package org.techascent.aifinder.data.location

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.location.LocationError
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Cache-first: reads always come from [local]. Firebase is only read when the user's cache is empty,
 * and every create writes to Firebase first, then to the cache.
 */
class LocationRepositoryImpl(
    private val remote: LocationRemoteDataSource,
    private val local: LocationLocalDataSource,
    private val auth: AuthRepository,
    private val now: () -> Long = ::currentTimeMillis,
) : LocationRepository {

    override fun observeLocations(): Flow<List<StorageLocation>> = flow {
        val userId = auth.currentUser?.id ?: throw LocationError.NotSignedIn
        if (local.isEmpty(userId)) {
            local.replaceAll(userId, remote.fetchAll(userId))
        }
        emitAll(local.observe(userId).map { nodes -> nodes.map { (id, dto) -> dto.toDomain(id) } })
    }.catch { throw it.asLocationError() }

    override suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation> {
        val userId = auth.currentUser?.id ?: return Result.failure(LocationError.NotSignedIn)
        return try {
            val dto = LocationDto.from(request, now())
            val id = remote.create(userId, dto)
            local.upsert(userId, id, dto)
            Result.success(dto.toDomain(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asLocationError())
        }
    }

    private fun Throwable.asLocationError() = this as? LocationError ?: LocationError.Remote(message)
}

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
