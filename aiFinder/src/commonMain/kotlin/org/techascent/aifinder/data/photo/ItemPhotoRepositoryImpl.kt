package org.techascent.aifinder.data.photo

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.photo.ItemPhotoRepository
import org.techascent.aifinder.domain.photo.PhotoError
import org.techascent.aifinder.util.logDebug

/**
 * Cache-first: photos are read from the device; cloud storage is only downloaded from when the local
 * copy is missing (e.g. a new device). Saving uploads first so the cloud copy always exists.
 */
class ItemPhotoRepositoryImpl(
    private val remote: PhotoRemoteDataSource,
    private val local: PhotoLocalDataSource,
    private val auth: AuthRepository,
    private val memoryLimit: Int = 24,
) : ItemPhotoRepository {
    private val lock = Mutex()
    private val memory = LinkedHashMap<String, ByteArray>()
    private val inFlight = mutableMapOf<String, CompletableDeferred<ByteArray?>>()

    override suspend fun save(photoId: String, jpegBytes: ByteArray): Result<Unit> {
        val userId = auth.currentUser?.id ?: return Result.failure(PhotoError.NotSignedIn)
        return try {
            remote.upload(userId, photoId, jpegBytes)
            cacheLocally(userId, photoId, jpegBytes)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e as? PhotoError ?: PhotoError.Remote(e.message))
        }
    }

    override suspend fun load(photoId: String): ByteArray? {
        val userId = auth.currentUser?.id ?: return null
        val key = "$userId/$photoId"
        // Items from one scan share a photo, so concurrent requests for it share one load.
        val (deferred, owner) = lock.withLock {
            memory[key]?.let { return it }
            inFlight[key]?.let { it to false } ?: CompletableDeferred<ByteArray?>().also { inFlight[key] = it }.let { it to true }
        }
        if (!owner) return deferred.await()
        val bytes = try {
            local.read(userId, photoId) ?: remote.download(userId, photoId)?.also {
                runCatching { local.write(userId, photoId, it) }
            }
        } catch (e: CancellationException) {
            lock.withLock { inFlight.remove(key) }
            deferred.complete(null)
            throw e
        } catch (e: Exception) {
            logDebug(TAG, "Couldn't load photo $photoId: ${e.message}")
            null
        }
        lock.withLock {
            inFlight.remove(key)
            if (bytes != null) remember(key, bytes)
        }
        deferred.complete(bytes)
        return bytes
    }

    private suspend fun cacheLocally(userId: String, photoId: String, bytes: ByteArray) {
        // The upload already succeeded; a failed local write only means a later re-download.
        runCatching { local.write(userId, photoId, bytes) }
            .onFailure { logDebug(TAG, "Couldn't cache photo $photoId: ${it.message}") }
        lock.withLock { remember("$userId/$photoId", bytes) }
    }

    private fun remember(key: String, bytes: ByteArray) {
        memory.remove(key)
        memory[key] = bytes
        while (memory.size > memoryLimit) memory.remove(memory.keys.first())
    }
}

private const val TAG = "AiFinderPhotos"
