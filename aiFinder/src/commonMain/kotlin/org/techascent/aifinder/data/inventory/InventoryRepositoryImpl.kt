package org.techascent.aifinder.data.inventory

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.inventory.InventoryError
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.model.ItemDetection
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Offline-first: reads always come from [local]. Firebase is only read when the user's cache is empty,
 * and a failed or slow read (e.g. offline) falls back to the cache instead of failing,
 * and every save writes to Firebase first, then to the cache.
 */
class InventoryRepositoryImpl(
    private val remote: ItemRemoteDataSource,
    private val local: ItemLocalDataSource,
    private val auth: AuthRepository,
    private val now: () -> Long = ::currentTimeMillis,
    private val remoteTimeoutMillis: Long = REMOTE_TIMEOUT_MILLIS,
) : InventoryRepository {

    override fun observeItems(): Flow<List<StoredItem>> = flow {
        val userId = auth.currentUser?.id ?: throw InventoryError.NotSignedIn
        if (local.isEmpty(userId)) syncFromRemote(userId)
        emitAll(local.observe(userId).map { nodes -> nodes.map { (id, dto) -> dto.toDomain(id) } })
    }.catch { throw it.asInventoryError() }

    override suspend fun saveDetections(
        locationId: String,
        detections: List<ItemDetection>,
    ): Result<List<StoredItem>> {
        val userId = auth.currentUser?.id ?: return Result.failure(InventoryError.NotSignedIn)
        return try {
            val createdAt = now()
            val created = remote.createAll(userId, detections.map { ItemDto.from(it, locationId, createdAt) })
            local.upsertAll(userId, created)
            Result.success(created.map { (id, dto) -> dto.toDomain(id) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asInventoryError())
        }
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

    private fun Throwable.asInventoryError() = this as? InventoryError ?: InventoryError.Remote(message)
}

private const val REMOTE_TIMEOUT_MILLIS = 10_000L

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
