package org.techascent.aifinder.data.auth

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.data.location.RealtimeDatabasePaths
import org.techascent.aifinder.util.Platform
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
    private val saved = mutableListOf<Pair<String, SignedInData>>()
    private val remote = object : SignInRemoteDataSource {
        override suspend fun save(userId: String, data: SignedInData) {
            saved += userId to data
        }
    }

    private fun repository(google: GoogleAuthDataSource, source: SignInRemoteDataSource = remote) =
        AuthRepositoryImpl(google, source, deviceName = { "Google Pixel" })

    private class FakeGoogle(private val result: () -> AuthUser) : GoogleAuthDataSource {
        override var currentUser: AuthUser? = null
        override suspend fun signIn(): AuthUser = result().also { currentUser = it }
        override suspend fun signOut() { currentUser = null }
    }

    @Test
    fun googleSignInReturnsUser() = runBlocking {
        val repo = repository(FakeGoogle { user })
        assertEquals(user, repo.signIn(AuthProvider.Google).getOrThrow())
        val (userId, metadata) = saved.single()
        assertEquals("uid", userId)
        assertEquals(Platform.ANDROID, metadata.platform)
        assertEquals(AuthProvider.Google, metadata.provider)
        assertEquals("Google Pixel", metadata.deviceName)
        assertEquals(user, repo.currentUser)
        repo.signOut()
        assertNull(repo.currentUser)
    }

    @Test
    fun appleIsUnavailable() = runBlocking {
        val error = repository(FakeGoogle { user }).signIn(AuthProvider.Apple).exceptionOrNull()
        assertEquals(AuthError.Unavailable, (error as AuthException).error)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun unexpectedFailuresMapToUnknown() = runBlocking {
        val error = repository(FakeGoogle { error("boom") }).signIn(AuthProvider.Google).exceptionOrNull()
        val authError = (error as AuthException).error
        assertTrue(authError is AuthError.Unknown)
        assertEquals("boom", authError.message)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun metadataFailureIsSurfaced() = runBlocking {
        val repo = repository(FakeGoogle { user }, object : SignInRemoteDataSource {
            override suspend fun save(userId: String, data: SignedInData) = error("Permission denied")
        })
        val error = repo.signIn(AuthProvider.Google).exceptionOrNull() as AuthException
        assertEquals(AuthError.SignInDataSaveFailed, error.error)
        assertEquals("Permission denied", error.cause?.message)
        assertTrue(error.error.toMessageRes() != null)
    }

    @Test
    fun stalledMetadataSaveTimesOut() = runTest {
        val repo = repository(FakeGoogle { user }, object : SignInRemoteDataSource {
            override suspend fun save(userId: String, data: SignedInData) {
                CompletableDeferred<Unit>().await()
            }
        })
        val error = repo.signIn(AuthProvider.Google).exceptionOrNull() as AuthException
        assertEquals(AuthError.SignInDataSaveFailed, error.error)
        assertEquals(15_000L, testScheduler.currentTime)
    }

    @Test
    fun metadataSaveCancellationIsPropagated() = runBlocking {
        val repo = repository(FakeGoogle { user }, object : SignInRemoteDataSource {
            override suspend fun save(userId: String, data: SignedInData) {
                throw CancellationException("Cancelled")
            }
        })
        kotlin.test.assertFailsWith<CancellationException> { repo.signIn(AuthProvider.Google) }
        Unit
    }

    @Test
    fun metadataUsesUserScopedPathAndStableFieldNames() {
        assertEquals("aifinder/users/uid/signedIn", RealtimeDatabasePaths.signedIn("uid"))
        assertEquals(
            mapOf("platform" to "IOS", "provider" to "Apple", "deviceName" to "iPhone"),
            SignedInData(Platform.IOS, AuthProvider.Apple, "iPhone").toMap()
        )
    }

    @Test
    fun cancelledSignInShowsNoMessage() {
        assertNull(AuthError.Cancelled.toMessageRes())
    }
}
