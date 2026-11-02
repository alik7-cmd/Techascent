package org.techascent.aifinder.data.inventory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.inventory.InventoryError
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InventoryRepositoryImplTest {
    private class FakeAuth(override var currentUser: AuthUser?) : AuthRepository {
        override suspend fun signIn(provider: AuthProvider) = Result.failure<AuthUser>(UnsupportedOperationException())
        override suspend fun signOut() { currentUser = null }
    }

    private class FakeRemote : ItemRemoteDataSource {
        val store = mutableMapOf<String, MutableList<Pair<String, ItemDto>>>()
        var fetchCount = 0
        var failWith: Exception? = null
        var hang = false

        override suspend fun fetchAll(userId: String): List<Pair<String, ItemDto>> {
            fetchCount++
            if (hang) kotlinx.coroutines.awaitCancellation()
            failWith?.let { throw it }
            return store[userId].orEmpty().toList()
        }

        override suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>> {
            failWith?.let { throw it }
            val list = store.getOrPut(userId) { mutableListOf() }
            val created = items.mapIndexed { index, dto -> "id-${list.size + index}" to dto }
            list += created
            return created
        }
    }

    private class FakeLocal : ItemLocalDataSource {
        val rows = MutableStateFlow<Map<String, List<Pair<String, ItemDto>>>>(emptyMap())
        override fun observe(userId: String): Flow<List<Pair<String, ItemDto>>> =
            rows.map { it[userId].orEmpty().sortedByDescending { (_, dto) -> dto.createdAt } }
        override suspend fun isEmpty(userId: String) = rows.value[userId].isNullOrEmpty()
        override suspend fun replaceAll(userId: String, items: List<Pair<String, ItemDto>>) {
            rows.value = rows.value + (userId to items)
        }
        override suspend fun upsertAll(userId: String, items: List<Pair<String, ItemDto>>) {
            val ids = items.map { it.first }.toSet()
            rows.value = rows.value + (userId to rows.value[userId].orEmpty().filterNot { it.first in ids } + items)
        }
    }

    private val user = AuthUser(id = "u1", displayName = "Ada", email = null, photoUrl = null)
    private val detection = ItemDetection(
        photoId = "photo",
        item = InventoryItem("temp-id", "Scissors", "Tools", listOf("scissors")),
        confidence = 0.9f,
    )

    @Test
    fun fetchesRemoteOnlyWhenCacheIsEmpty() = runBlocking<Unit> {
        val remote = FakeRemote().apply {
            store["u1"] = mutableListOf("a" to ItemDto("Hammer", "Tools", emptyList(), "loc", null, null, 5))
        }
        val repository = InventoryRepositoryImpl(remote, FakeLocal(), FakeAuth(user))

        assertEquals(listOf("Hammer"), repository.observeItems().first().map { it.item.name })
        assertEquals(listOf("Hammer"), repository.observeItems().first().map { it.item.name })
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun offlineOrSlowFirebaseFallsBackToCache() = runBlocking<Unit> {
        val failing = FakeRemote().apply { failWith = IllegalStateException("Client is offline") }
        assertTrue(InventoryRepositoryImpl(failing, FakeLocal(), FakeAuth(user)).observeItems().first().isEmpty())

        val hanging = FakeRemote().apply { hang = true }
        val repository = InventoryRepositoryImpl(hanging, FakeLocal(), FakeAuth(user), remoteTimeoutMillis = 50)
        assertTrue(repository.observeItems().first().isEmpty())
        assertEquals(1, hanging.fetchCount)
    }

    @Test
    fun saveWritesRemoteThenCacheWithGeneratedIds() = runBlocking<Unit> {
        val remote = FakeRemote()
        val local = FakeLocal()
        val repository = InventoryRepositoryImpl(remote, local, FakeAuth(user), now = { 42 })

        val saved = repository.saveDetections("loc", listOf(detection)).getOrThrow()

        assertEquals("id-0", saved.single().item.id)
        assertEquals("loc", saved.single().locationId)
        assertEquals(ItemDto("Scissors", "Tools", listOf("scissors"), "loc", "photo", 0.9f, 42), remote.store["u1"]!!.single().second)
        assertEquals(listOf("id-0"), repository.observeItems().first().map { it.item.id })
        assertEquals(0, remote.fetchCount)
    }

    @Test
    fun failedRemoteSaveDoesNotTouchCache() = runBlocking<Unit> {
        val remote = FakeRemote().apply { failWith = IllegalStateException("Permission denied") }
        val local = FakeLocal()
        val result = InventoryRepositoryImpl(remote, local, FakeAuth(user)).saveDetections("loc", listOf(detection))

        val error = assertIs<InventoryError.Remote>(result.exceptionOrNull())
        assertEquals("Permission denied", error.message)
        assertTrue(local.isEmpty("u1"))
    }

    @Test
    fun requiresSignedInUser() = runBlocking<Unit> {
        val repository = InventoryRepositoryImpl(FakeRemote(), FakeLocal(), FakeAuth(null))
        assertFailsWith<InventoryError.NotSignedIn> { repository.observeItems().first() }
        assertIs<InventoryError.NotSignedIn>(repository.saveDetections("loc", listOf(detection)).exceptionOrNull())
    }

    @Test
    fun cacheIsPerUser() = runBlocking<Unit> {
        val auth = FakeAuth(user)
        val local = FakeLocal()
        val repository = InventoryRepositoryImpl(FakeRemote(), local, auth)
        repository.saveDetections("loc", listOf(detection)).getOrThrow()

        auth.currentUser = AuthUser(id = "u2", displayName = "Bob", email = null, photoUrl = null)
        assertTrue(repository.observeItems().first().isEmpty())
    }

    @Test
    fun malformedRemoteNodesAreSkipped() {
        assertEquals(null, ItemDto.fromMap(mapOf("name" to "No location")))
        assertEquals(null, ItemDto.fromMap(mapOf("locationId" to "loc")))
        val dto = ItemDto.fromMap(mapOf("name" to "Cable", "locationId" to "loc", "tags" to listOf("hdmi", 3), "confidence" to 0.5))
        assertEquals(ItemDto("Cable", "General", listOf("hdmi"), "loc", null, 0.5f, 0), dto)
    }
}
