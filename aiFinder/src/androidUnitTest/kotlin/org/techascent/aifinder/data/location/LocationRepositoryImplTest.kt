package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
        var failWith: Exception? = null

        override fun observe(userId: String): Flow<List<Pair<String, LocationDto>>> = flow {
            failWith?.let { throw it }
            emit(store[userId].orEmpty().toList())
        }

        override suspend fun create(userId: String, location: LocationDto): String {
            failWith?.let { throw it }
            val list = store.getOrPut(userId) { mutableListOf() }
            val id = "id-${list.size}"
            list += id to location
            return id
        }
    }

    private val user = AuthUser("uid", null, null, null)

    @Test
    fun createsAndObservesLocationsSortedByCreation() = runBlocking<Unit> {
        val remote = FakeRemote()
        var clock = 200L
        val repo = LocationRepositoryImpl(remote, FakeAuth(user), now = { clock-- })
        val garage = repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).getOrThrow()
        repo.createLocation(CreateLocationRequest("Shelf", LocationType.CONTAINER, garage.id)).getOrThrow()

        val locations = repo.observeLocations().first()
        assertEquals(listOf("Shelf", "Garage"), locations.map { it.name })
        assertEquals(garage.id, locations.first().parentId)
        assertTrue(remote.store.keys == setOf("uid"))
    }

    @Test
    fun requiresSignedInUser() = runBlocking<Unit> {
        val repo = LocationRepositoryImpl(FakeRemote(), FakeAuth(null))
        assertIs<LocationError.NotSignedIn>(
            repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).exceptionOrNull()
        )
        assertFailsWith<LocationError.NotSignedIn> { repo.observeLocations().first() }
    }

    @Test
    fun mapsRemoteFailures() = runBlocking<Unit> {
        val remote = FakeRemote().apply { failWith = IllegalStateException("Permission denied") }
        val repo = LocationRepositoryImpl(remote, FakeAuth(user))
        val error = repo.createLocation(CreateLocationRequest("Garage", LocationType.AREA)).exceptionOrNull()
        assertIs<LocationError.Remote>(error)
        assertEquals("Permission denied", error.message)
        assertFailsWith<LocationError.Remote> { repo.observeLocations().first() }
    }

    @Test
    fun useCaseValidatesBeforeWriting() = runBlocking<Unit> {
        val remote = FakeRemote()
        val createLocation = CreateLocationUseCase(LocationRepositoryImpl(remote, FakeAuth(user)))
        val result = createLocation(CreateLocationRequest(" ", LocationType.ROOM), existing = emptyList())
        assertIs<LocationError.BlankName>(result.exceptionOrNull())
        assertTrue(remote.store.isEmpty())
    }
}
