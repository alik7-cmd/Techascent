package org.techascent.aifinder.data.location

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.location.LocationError
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Offline-first: reads always come from [local]. Firebase is only read when the user's cache is empty,
 * and a failed or slow read (e.g. offline) falls back to the cache instead of failing,
 * and every create writes to Firebase first, then to the cache.
 */
class LocationRepositoryImpl(
    private val remote: LocationRemoteDataSource,
    private val local: LocationLocalDataSource,
    private val auth: AuthRepository,
    private val now: () -> Long = ::currentTimeMillis,
    private val remoteTimeoutMillis: Long = REMOTE_TIMEOUT_MILLIS,
) : LocationRepository {
    private val mutations = Mutex()

    override suspend fun refresh(): Result<Unit> = mutations.withLock {
        val userId = auth.currentUser?.id ?: return@withLock Result.failure(LocationError.NotSignedIn)
        try {
            val nodes = withTimeoutOrNull(remoteTimeoutMillis) { remote.fetchAll(userId) }
                ?: error("Refreshing places timed out. Try again.")
            check(auth.currentUser?.id == userId) { "The signed-in user changed." }
            local.replaceAll(userId, nodes)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asLocationError())
        }
    }

    override fun observeLocations(): Flow<List<StorageLocation>> = flow {
        val userId = auth.currentUser?.id ?: throw LocationError.NotSignedIn
        if (local.isEmpty(userId)) syncFromRemote(userId)
        emitAll(local.observe(userId).map { nodes -> nodes.map { (id, dto) -> dto.toDomain(id) } })
    }.catch { throw it.asLocationError() }

    override suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation> {
        val userId = auth.currentUser?.id ?: return Result.failure(LocationError.NotSignedIn)
        return mutations.withLock { try {
            val dto = LocationDto.from(request, now())
            val id = remote.create(userId, dto)
            check(auth.currentUser?.id == userId) { "The signed-in user changed." }
            local.upsert(userId, id, dto)
            Result.success(dto.toDomain(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asLocationError())
        } }
    }

    private suspend fun syncFromRemote(userId: String) {
        val nodes = try {
            withTimeoutOrNull(remoteTimeoutMillis) { remote.fetchAll(userId) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: return
        local.replaceAll(userId, nodes)
    }

    private fun Throwable.asLocationError() = this as? LocationError ?: LocationError.Remote(message)
}

private const val REMOTE_TIMEOUT_MILLIS = 10_000L

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
