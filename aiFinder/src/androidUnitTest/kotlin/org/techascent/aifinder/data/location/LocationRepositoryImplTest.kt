package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.location.LocationError
import org.techascent.aifinder.domain.location.usecase.CreateLocationUseCase
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LocationRepositoryImplTest {
    private class FakeAuth(override var currentUser: AuthUser?) : AuthRepository {
        override suspend fun signIn(provider: AuthProvider) = Result.failure<AuthUser>(UnsupportedOperationException())
        override suspend fun signOut() { currentUser = null }
    }

    private class FakeRemote : LocationRemoteDataSource {
        val store = mutableMapOf<String, MutableList<Pair<String, LocationDto>>>()
        var fetchCount = 0
        var failWith: Exception? = null

        override suspend fun fetchAll(userId: String): List<Pair<String, LocationDto>> {
            fetchCount++
            failWith?.let { throw it }
            return store[userId].orEmpty().toList()
        }

        override suspend fun create(userId: String, location: LocationDto): String {
            failWith?.let { throw it }
            val list = store.getOrPut(userId) { mutableListOf() }
            val id = "id-${list.size}"
            list += id to location
            return id
        }
    }

    private class FakeLocal : LocationLocalDataSource {
        val rows = MutableStateFlow<Map<String, List<Pair<String, LocationDto>>>>(emptyMap())
        override fun observe(userId: String): Flow<List<Pair<String, LocationDto>>> =
            rows.map { it[userId].orEmpty().sortedBy { (_, dto) -> dto.createdAt } }
        override suspend fun isEmpty(userId: String) = rows.value[userId].isNullOrEmpty()
        override suspend fun replaceAll(userId: String, nodes: List<Pair<String, LocationDto>>) {
            rows.value = rows.value + (userId to nodes)
        }
        override suspend fun upsert(userId: String, id: String, location: LocationDto) {
            val current = rows.value[userId].orEmpty().filterNot { it.first == id }
            rows.value = rows.value + (userId to current + (id to location))
        }
    }

    private val user = AuthUser("uid", null, null, null)

    @Test
    fun emptyCacheFetchesFromFirebaseOnceThenServesFromCache() = runBlocking<Unit> {
        val remote = FakeRemote().apply {
            store["uid"] = mutableListOf("r1" to LocationDto("Garage", "AREA", null, 1))
        }
        val local = FakeLocal()
        val repo = LocationRepositoryImpl(remote, local, FakeAuth(user))

        assertEquals(listOf("Garage"), repo.observeLocations().first().map { it.name })
        assertEquals(1, remote.fetchCount)

        remote.store["uid"]!!.add("r2" to LocationDto("Remote only", "ROOM", null, 2))
        assertEquals(listOf("Garage"), repo.observeLocations().first().map { it.name })
        assertEquals(1, remote.fetchCount, "A filled cache must not hit Firebase")
    }

    @Test
    fun createWritesToFirebaseAndCache() = runBlocking<Unit> {
        val remote = FakeRemote()
        val local = FakeLocal()
        var clock = 100L
        val repo = LocationRepositoryImpl(remote, local, FakeAuth(user), now = { clock++ })

        val garage = repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).getOrThrow()
        repo.createLocation(CreateLocationRequest("Shelf", LocationType.CONTAINER, garage.id)).getOrThrow()

        assertEquals(2, remote.store["uid"]!!.size)
        val cached = repo.observeLocations().first()
        assertEquals(listOf("Garage", "Shelf"), cached.map { it.name })
        assertEquals(garage.id, cached.last().parentId)
        assertEquals(0, remote.fetchCount)
    }

    @Test
    fun failedFirebaseWriteDoesNotTouchCache() = runBlocking<Unit> {
        val remote = FakeRemote().apply { failWith = IllegalStateException("Permission denied") }
        val local = FakeLocal()
        val repo = LocationRepositoryImpl(remote, local, FakeAuth(user))
        val error = repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).exceptionOrNull()
        assertIs<LocationError.Remote>(error)
        assertEquals("Permission denied", error.message)
        assertTrue(local.isEmpty("uid"))
        assertFailsWith<LocationError.Remote> { repo.observeLocations().first() }
    }

    @Test
    fun requiresSignedInUser() = runBlocking<Unit> {
        val repo = LocationRepositoryImpl(FakeRemote(), FakeLocal(), FakeAuth(null))
        assertIs<LocationError.NotSignedIn>(
            repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).exceptionOrNull()
        )
        assertFailsWith<LocationError.NotSignedIn> { repo.observeLocations().first() }
    }

    @Test
    fun useCaseValidatesBeforeWriting() = runBlocking<Unit> {
        val remote = FakeRemote()
        val createLocation = CreateLocationUseCase(LocationRepositoryImpl(remote, FakeLocal(), FakeAuth(user)))
        val result = createLocation(CreateLocationRequest(" ", LocationType.ROOM), existing = emptyList())
        assertIs<LocationError.BlankName>(result.exceptionOrNull())
        assertTrue(remote.store.isEmpty())
    }
}
