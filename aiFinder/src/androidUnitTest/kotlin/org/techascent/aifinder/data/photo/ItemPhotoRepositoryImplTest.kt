package org.techascent.aifinder.data.photo

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.photo.PhotoError
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ItemPhotoRepositoryImplTest {
    @get:Rule
    val folder = TemporaryFolder()

    private class FakeAuth(override var currentUser: AuthUser?) : AuthRepository {
        override suspend fun signIn(provider: AuthProvider) = Result.failure<AuthUser>(UnsupportedOperationException())
        override suspend fun signOut() { currentUser = null }
    }

    private class FakeRemote : PhotoRemoteDataSource {
        val store = mutableMapOf<String, ByteArray>()
        var downloads = 0
        var failUpload: Exception? = null
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun upload(userId: String, photoId: String, jpegBytes: ByteArray) {
            failUpload?.let { throw it }
            store[StoragePaths.photo(userId, photoId)] = jpegBytes
        }

        override suspend fun download(userId: String, photoId: String): ByteArray? {
            downloads++
            gate?.await()
            return store[StoragePaths.photo(userId, photoId)]
        }
    }

    private val user = AuthUser("u1", "Ada", null, null)
    private val bytes = byteArrayOf(1, 2, 3)

    private fun repository(remote: FakeRemote, auth: AuthRepository = FakeAuth(user)) =
        ItemPhotoRepositoryImpl(remote, FilePhotoLocalDataSource(folder.root), auth)

    @Test
    fun saveUploadsThenServesFromDeviceWithoutDownloading() = runBlocking<Unit> {
        val remote = FakeRemote()
        repository(remote).save("photo-1", bytes).getOrThrow()

        assertContentEquals(bytes, remote.store["aifinder/users/u1/photos/photo-1.jpg"])
        // A fresh repository has no memory cache, so this proves the file cache is used.
        assertContentEquals(bytes, repository(remote).load("photo-1"))
        assertEquals(0, remote.downloads)
    }

    @Test
    fun missingLocalPhotoIsDownloadedOnceAndCached() = runBlocking<Unit> {
        val remote = FakeRemote().apply { store[StoragePaths.photo("u1", "p")] = bytes }
        assertContentEquals(bytes, repository(remote).load("p"))
        assertContentEquals(bytes, repository(remote).load("p"))
        assertEquals(1, remote.downloads)
    }

    @Test
    fun concurrentLoadsShareOneDownload() = runBlocking<Unit> {
        val remote = FakeRemote().apply {
            store[StoragePaths.photo("u1", "p")] = bytes
            gate = CompletableDeferred()
        }
        val repo = repository(remote)
        val loads = List(3) { async { repo.load("p") } }
        kotlinx.coroutines.yield()
        remote.gate!!.complete(Unit)
        loads.forEach { assertContentEquals(bytes, it.await()) }
        assertEquals(1, remote.downloads)
    }

    @Test
    fun failedUploadIsNotCachedAndMissingPhotoIsNull() = runBlocking<Unit> {
        val remote = FakeRemote().apply { failUpload = IllegalStateException("denied") }
        val result = repository(remote).save("p", bytes)
        assertEquals("denied", assertIs<PhotoError.Remote>(result.exceptionOrNull()).message)
        assertNull(repository(remote).load("p"))
    }

    @Test
    fun photosAreScopedToTheSignedInUser() = runBlocking<Unit> {
        val remote = FakeRemote()
        val auth = FakeAuth(user)
        repository(remote, auth).save("p", bytes).getOrThrow()
        auth.currentUser = AuthUser("u2", "Bob", null, null)
        assertNull(repository(remote, auth).load("p"))
        auth.currentUser = null
        assertIs<PhotoError.NotSignedIn>(repository(remote, auth).save("p", bytes).exceptionOrNull())
    }
}
