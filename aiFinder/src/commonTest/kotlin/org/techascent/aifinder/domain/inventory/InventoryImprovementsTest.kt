package org.techascent.aifinder.domain.inventory

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.data.inventory.*
import org.techascent.aifinder.domain.auth.*
import org.techascent.aifinder.domain.model.*
import kotlin.test.*

class InventoryImprovementsTest {
    private val place = StorageLocation("p", "Office", LocationType.ROOM)

    @Test
    fun searchMatchesAliasesSynonymsAndTyposButNotDifferentNumbers() {
        val snapshot = buildSnapshot(listOf(place), listOf(
            StoredItem(InventoryItem("1", "Power adapter", "Electronics", listOf("travel plug")), "p", 1),
            StoredItem(InventoryItem("2", "Passport", "Documents"), "p", 2),
            StoredItem(InventoryItem("3", "Box 12345", "Storage"), "p", 3),
        ))
        assertEquals(listOf("1"), snapshot.search("Where is my charger").map { it.item.id })
        assertEquals(listOf("1"), snapshot.search("travel plug").map { it.item.id })
        assertEquals(listOf("2"), snapshot.search("pasport").map { it.item.id })
        assertEquals(listOf("2"), snapshot.search("passprot").map { it.item.id })
        assertTrue(snapshot.search("12346").isEmpty())
        assertTrue(snapshot.search("passport kitchen").isEmpty())
    }

    @Test
    fun dtoRoundTripsNotesFreshnessAndBounds() {
        val dto = ItemDto("Passport", "Documents", listOf("travel"), "p", "photo", 0.9f, 10,
            "Blue cover", 25, listOf(0.1f, 0.2f, 0.3f, 0.4f))
        assertEquals(dto, ItemDto.fromMap(dto.toMap()))
        val located = buildSnapshot(listOf(place), listOf(dto.toDomain("id"))).items.single()
        assertEquals(25L, located.confirmedAt)
        assertEquals("Blue cover", located.item.description)
        assertEquals(dto.bounds, located.bounds)
    }

    @Test
    fun mutationsWriteRemoteBeforeCacheAndFailuresKeepCache() = runTest {
        val calls = mutableListOf<String>()
        var fail = false
        val dto = ItemDto("Old", "General", emptyList(), "p", null, null, 1)
        val local = object : ItemLocalDataSource {
            override fun observe(userId: String): Flow<List<Pair<String, ItemDto>>> = flowOf(listOf("id" to dto))
            override suspend fun isEmpty(userId: String) = false
            override suspend fun replaceAll(userId: String, items: List<Pair<String, ItemDto>>) { calls += "replace" }
            override suspend fun upsertAll(userId: String, items: List<Pair<String, ItemDto>>) { calls += "cache" }
            override suspend fun delete(userId: String, itemId: String) { calls += "cache-delete" }
        }
        val remote = object : ItemRemoteDataSource {
            override suspend fun fetchAll(userId: String) = listOf("id" to dto)
            override suspend fun createAll(userId: String, items: List<ItemDto>) = items.map { "new" to it }
            override suspend fun update(userId: String, itemId: String, item: ItemDto) {
                calls += "remote"
                if (fail) error("Offline")
            }
            override suspend fun delete(userId: String, itemId: String) {
                calls += "remote-delete"
                if (fail) error("Offline")
            }
        }
        val auth = object : AuthRepository {
            override val currentUser = AuthUser("user", null, null, null)
            override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Unused")
            override suspend fun signOut() = Unit
        }
        val repo = InventoryRepositoryImpl(remote, local, auth)
        assertTrue(repo.update(dto.toDomain("id")).isSuccess)
        assertEquals(listOf("remote", "cache"), calls)
        calls.clear()
        fail = true
        assertTrue(repo.delete("id").isFailure)
        assertEquals(listOf("remote-delete"), calls)
        calls.clear()
        fail = false
        assertTrue(repo.delete("id").isSuccess)
        assertEquals(listOf("remote-delete", "cache-delete"), calls)
    }
}
