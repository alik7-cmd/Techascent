package org.techascent.aifinder.data.inventory

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.inventory.InventoryError
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.model.ItemDetection
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
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
    private val mutations = Mutex()
    override suspend fun clearPhotoReferences(photoId: String): Result<Unit> = mutate { userId ->
        val items = remote.fetchAll(userId).filter { it.second.photoId == photoId }
            .map { it.first to it.second.copy(photoId = null, bounds = null) }
        checkUser(userId)
        if (items.isNotEmpty()) {
            remote.clearPhotos(userId, items.map { it.first })
            checkUser(userId)
            local.upsertAll(userId, items)
        }
    }

    override suspend fun update(item: StoredItem): Result<Unit> = mutate { userId ->
        require(item.item.name.isNotBlank()) { "Enter an item name." }
        require(item.locationId.isNotBlank()) { "Choose a place." }
        val dto = ItemDto(
            item.item.name.trim(), item.item.category, item.item.tags, item.locationId,
            item.item.photoId, null, item.createdAt, item.item.description, item.confirmedAt, item.bounds,
        )
        remote.update(userId, item.item.id, dto)
        checkUser(userId)
        local.upsertAll(userId, listOf(item.item.id to dto))
    }

    override suspend fun delete(itemId: String): Result<Unit> = mutate { userId ->
        remote.delete(userId, itemId)
        checkUser(userId)
        local.delete(userId, itemId)
    }

    override suspend fun refresh(): Result<Unit> = mutate { userId ->
        val items = remote.fetchAll(userId)
        checkUser(userId)
        local.replaceAll(userId, items)
    }

    private suspend fun mutate(action: suspend (String) -> Unit): Result<Unit> = mutations.withLock {
        try {
            val userId = auth.currentUser?.id ?: throw InventoryError.NotSignedIn
            withTimeout(remoteTimeoutMillis) { action(userId) }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(InventoryError.Remote("Updating inventory timed out. Try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asInventoryError())
        }
    }

    private fun checkUser(userId: String) {
        check(auth.currentUser?.id == userId) { "The signed-in user changed. Please try again." }
    }

    override fun observeItems(): Flow<List<StoredItem>> = flow {
        val userId = auth.currentUser?.id ?: throw InventoryError.NotSignedIn
        if (local.isEmpty(userId)) syncFromRemote(userId)
        emitAll(local.observe(userId).map { nodes -> nodes.map { (id, dto) -> dto.toDomain(id) } })
    }.catch { throw it.asInventoryError() }

    override suspend fun saveDetections(
        locationId: String,
        detections: List<ItemDetection>,
    ): Result<List<StoredItem>> = saveReviewed(locationId, detections, emptyMap())

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override suspend fun saveReviewed(
        locationId: String,
        detections: List<ItemDetection>,
        replacements: Map<String, StoredItem>,
    ): Result<List<StoredItem>> {
        val userId = auth.currentUser?.id ?: return Result.failure(InventoryError.NotSignedIn)
        return mutations.withLock { try {
            val createdAt = now()
            val created = detections.map { detection ->
                val previous = replacements[detection.item.id]
                val id = previous?.item?.id ?: if (detection.photoId.isNotBlank()) {
                    // Same reviewed photo/item writes the same key on retry, even after an uncertain network response.
                    (detection.photoId + ":" + detection.item.id).encodeToByteArray()
                        .joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
                        .also { require(it.length <= 768) { "Scan item identifier is too long." } }
                } else {
                    val draft = detection.item.id.ifBlank { kotlin.uuid.Uuid.random().toString() }
                    require(draft.length <= 700 && draft.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
                        "Invalid manual item identifier."
                    }
                    "manual-$draft"
                }
                id to ItemDto.from(detection, locationId, createdAt).copy(
                    createdAt = previous?.createdAt ?: createdAt,
                    confirmedAt = createdAt,
                    description = previous?.item?.description ?: detection.item.description,
                    tags = (previous?.item?.tags.orEmpty() + detection.item.tags).distinct(),
                )
            }
            withTimeout(remoteTimeoutMillis) { remote.writeAll(userId, created) }
            checkUser(userId)
            local.upsertAll(userId, created)
            Result.success(created.map { (id, dto) -> dto.toDomain(id) })
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(InventoryError.Remote("Saving inventory timed out. Retry with the same reviewed photo."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.asInventoryError())
        } }
    }

    private suspend fun syncFromRemote(userId: String) {
        val nodes = try {
            withTimeoutOrNull(remoteTimeoutMillis.milliseconds) { remote.fetchAll(userId) }
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
