package org.techascent.aifinder.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.util.Platform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IosAuthBridgeTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun signInAndRestoredSessionUseFirebaseUser() = runTest {
        val bridge = FakeBridge()
        val source = IosGoogleAuthDataSource(bridge)
        assertNull(source.currentUser)
        assertEquals(user, source.signIn())
        assertEquals(user, source.currentUser)
    }

    @Test
    fun errorsMapToSharedAuthErrors() = runTest {
        val errors = listOf(
            IosAuthFailureKind.Cancelled to AuthError.Cancelled,
            IosAuthFailureKind.Network to AuthError.Network,
            IosAuthFailureKind.Unavailable to AuthError.Unavailable,
            IosAuthFailureKind.Unknown to AuthError.Unknown("Failed"),
        )
        errors.forEach { (kind, expected) ->
            val source = IosGoogleAuthDataSource(FakeBridge(failure = IosAuthFailure(kind, "Failed")))
            assertEquals(expected, assertFailsWith<AuthException> { source.signIn() }.error)
        }
    }

    @Test
    fun missingUserIsAnExplicitFailure() = runTest {
        val bridge = FakeBridge()
        bridge.signInAction = { it(null, null) }
        assertFailsWith<AuthException> { IosGoogleAuthDataSource(bridge).signIn() }
    }

    @Test
    fun signOutClearsSessionAndPropagatesFailures() = runTest {
        val bridge = FakeBridge()
        val source = IosGoogleAuthDataSource(bridge)
        source.signIn()
        source.signOut()
        assertNull(source.currentUser)
        bridge.failure = IosAuthFailure(IosAuthFailureKind.Unknown, "Keychain failure")
        assertFailsWith<AuthException> { source.signOut() }
    }

    @Test
    fun metadataUsesFirebaseUidAndExpectedFields() = runTest {
        val bridge = FakeBridge()
        IosSignInDataSource(bridge).save(user.id, SignedInData(Platform.IOS, AuthProvider.Google, "iPhone"))
        assertEquals(user.id, bridge.metadataUserId)
        assertEquals(
            mapOf("platform" to "IOS", "provider" to "Google", "deviceName" to "iPhone"),
            bridge.metadata,
        )
        bridge.failure = IosAuthFailure(IosAuthFailureKind.Unknown, "Permission denied")
        assertFailsWith<IllegalStateException> {
            IosSignInDataSource(bridge).save(user.id, SignedInData(Platform.IOS, AuthProvider.Google, "iPhone"))
        }
    }

    @Test
    fun completionAfterCancellationDoesNotResumeCaller() = runTest {
        val bridge = FakeBridge()
        lateinit var complete: (AuthUser?, IosAuthFailure?) -> Unit
        bridge.signInAction = { complete = it }
        val pending = async { IosGoogleAuthDataSource(bridge).signIn() }
        runCurrent()
        pending.cancel()
        runCurrent()
        complete(user, null)
        assertTrue(pending.isCancelled)
    }

    private class FakeBridge(var failure: IosAuthFailure? = null) : IosAuthBridge {
        override var currentUser: AuthUser? = null
        var metadataUserId: String? = null
        var metadata: Map<String, String>? = null
        var signInAction: (((AuthUser?, IosAuthFailure?) -> Unit) -> Unit)? = null

        override fun signIn(completion: (AuthUser?, IosAuthFailure?) -> Unit) {
            signInAction?.let { it(completion); return }
            if (failure == null) currentUser = user
            completion(currentUser, failure)
        }

        override fun signOut(completion: (IosAuthFailure?) -> Unit) {
            if (failure == null) currentUser = null
            completion(failure)
        }

        override fun saveSignInMetadata(
            userId: String,
            data: Map<String, String>,
            completion: (IosAuthFailure?) -> Unit,
        ) {
            metadataUserId = userId
            metadata = data
            completion(failure)
        }
    }

    private companion object {
        val user = AuthUser("firebase-uid", "Name", "user@example.com", null, AuthProvider.Google)
    }
}
