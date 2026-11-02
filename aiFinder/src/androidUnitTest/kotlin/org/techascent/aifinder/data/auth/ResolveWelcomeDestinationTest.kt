package org.techascent.aifinder.data.auth

import kotlinx.coroutines.runBlocking
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.auth.ResolveWelcomeDestinationUseCase
import org.techascent.aifinder.domain.auth.WelcomeDestination
import org.techascent.aifinder.domain.auth.WelcomeRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ResolveWelcomeDestinationTest {
    private class FakeAuth(override val currentUser: AuthUser? = null) : AuthRepository {
        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Not used")
        override suspend fun signOut() = Unit
    }

    private class FakeWelcome(var seen: Boolean = false) : WelcomeRepository {
        override suspend fun hasSeenWelcome() = seen
        override suspend fun markWelcomeSeen() { seen = true }
    }

    @Test
    fun signedInGoesHomeRegardlessOfWelcomeFlag() = runBlocking<Unit> {
        for (seen in listOf(false, true)) {
            val resolve = ResolveWelcomeDestinationUseCase(
                FakeAuth(AuthUser("uid", null, null, null)), FakeWelcome(seen),
            )
            assertEquals(WelcomeDestination.Home, resolve())
        }
    }

    @Test
    fun firstSignedOutVisitShowsWelcomeAndFollowingVisitsGoToLogin() = runBlocking<Unit> {
        val welcome = FakeWelcome()
        val resolve = ResolveWelcomeDestinationUseCase(FakeAuth(), welcome)
        assertEquals(WelcomeDestination.Welcome, resolve())
        assertTrue(welcome.seen)
        assertEquals(WelcomeDestination.Login, resolve())
        assertEquals(
            WelcomeDestination.Login,
            ResolveWelcomeDestinationUseCase(FakeAuth(), welcome)(),
        )
    }

    @Test
    fun persistenceFailureIsNotTreatedAsFirstVisit() = runBlocking<Unit> {
        val welcome = object : WelcomeRepository {
            override suspend fun hasSeenWelcome(): Boolean = error("Database unavailable")
            override suspend fun markWelcomeSeen() = error("Must not be called")
        }
        assertFailsWith<IllegalStateException> {
            ResolveWelcomeDestinationUseCase(FakeAuth(), welcome)()
        }
    }
}
