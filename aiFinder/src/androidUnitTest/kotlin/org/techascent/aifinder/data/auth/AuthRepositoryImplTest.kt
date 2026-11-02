package org.techascent.aifinder.data.auth

import kotlinx.coroutines.runBlocking
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.presentation.login.toMessageRes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private val user = AuthUser("uid", "Ada", "ada@example.com", null)

    private class FakeGoogle(private val result: () -> AuthUser) : GoogleAuthDataSource {
        override var currentUser: AuthUser? = null
        override suspend fun signIn(): AuthUser = result().also { currentUser = it }
        override suspend fun signOut() { currentUser = null }
    }

    @Test
    fun googleSignInReturnsUser() = runBlocking {
        val repo = AuthRepositoryImpl(FakeGoogle { user })
        assertEquals(user, repo.signIn(AuthProvider.Google).getOrThrow())
        assertEquals(user, repo.currentUser)
        repo.signOut()
        assertNull(repo.currentUser)
    }

    @Test
    fun appleIsUnavailable() = runBlocking {
        val error = AuthRepositoryImpl(FakeGoogle { user }).signIn(AuthProvider.Apple).exceptionOrNull()
        assertEquals(AuthError.Unavailable, (error as AuthException).error)
    }

    @Test
    fun unexpectedFailuresMapToUnknown() = runBlocking {
        val error = AuthRepositoryImpl(FakeGoogle { error("boom") }).signIn(AuthProvider.Google).exceptionOrNull()
        val authError = (error as AuthException).error
        assertTrue(authError is AuthError.Unknown)
        assertEquals("boom", authError.message)
    }

    @Test
    fun cancelledSignInShowsNoMessage() {
        assertNull(AuthError.Cancelled.toMessageRes())
    }
}
