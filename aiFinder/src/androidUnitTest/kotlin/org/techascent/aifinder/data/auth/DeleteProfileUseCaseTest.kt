package org.techascent.aifinder.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.auth.usecase.DeleteProfileUseCase
import org.techascent.aifinder.domain.auth.usecase.ProfileDeletionRemote
import kotlin.test.*

class DeleteProfileUseCaseTest {
    private val events = mutableListOf<String>()
    private var signOutFails = false
    private val auth = object : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("alice", null, null, null)
        override suspend fun signIn(provider: AuthProvider) = error("Not used")
        override suspend fun signOut() {
            events += "signOut"
            if (signOutFails) error("Sign-out failed")
            currentUser = null
        }
    }

    @Test
    fun clearsLocalDataAndSignsOutOnlyAfterConfirmedRemoteDeletion() = runTest {
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote { events += "remote:$it" }) {
            events += "cache:$it"
        }
        delete()
        assertEquals(listOf("cache:alice", "remote:alice", "cache:alice", "signOut"), events)
        assertNull(auth.currentUser)
    }

    @Test
    fun remoteFailureKeepsSessionAndCanBeRetried() = runTest {
        var fail = true
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote {
            events += "remote"
            if (fail) error("Photo deletion failed")
        }) { events += "cache" }
        assertFailsWith<IllegalStateException> { delete() }
        assertNotNull(auth.currentUser)
        assertFalse("signOut" in events)
        fail = false
        delete()
        assertEquals(2, events.count { it == "remote" })
        assertNull(auth.currentUser)
    }

    @Test
    fun localCleanupFailureNeverStartsRemoteDeletion() = runTest {
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote { events += "remote" }) {
            error("Could not clear photos")
        }
        assertFailsWith<IllegalStateException> { delete() }
        assertTrue(events.isEmpty())
    }

    @Test
    fun signOutRetryDoesNotCallDeletedAccountEndpointAgain() = runTest {
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote { events += "remote" }) {
            events += "cache"
        }
        signOutFails = true
        assertFailsWith<IllegalStateException> { delete() }
        signOutFails = false
        delete()
        assertEquals(1, events.count { it == "remote" })
        assertNull(auth.currentUser)
    }

    @Test
    fun cancellationIsNotConvertedToSuccess() = runTest {
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote { throw CancellationException() }) {}
        assertFailsWith<CancellationException> { delete() }
        assertNotNull(auth.currentUser)
    }

    @Test
    fun signedOutRequestsFailBeforeAnyDeletion() = runTest {
        auth.currentUser = null
        val delete = DeleteProfileUseCase(auth, ProfileDeletionRemote { events += "remote" }) {}
        assertFailsWith<IllegalStateException> { delete() }
        assertTrue(events.isEmpty())
    }
}
