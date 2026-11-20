package org.techascent.aifinder.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthUser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class IosAuthFailureKind { Cancelled, Network, Unavailable, Unknown }

data class IosAuthFailure(val kind: IosAuthFailureKind, val message: String)

/** Implemented by the Swift host. All operations and completions run on the main thread. */
interface IosAuthBridge {
    val currentUser: AuthUser?
    fun signIn(completion: (AuthUser?, IosAuthFailure?) -> Unit)
    fun signOut(completion: (IosAuthFailure?) -> Unit)
    fun saveSignInMetadata(userId: String, data: Map<String, String>, completion: (IosAuthFailure?) -> Unit)
}

object IosAuthBridgeRegistry {
    private var bridge: IosAuthBridge? = null

    fun install(bridge: IosAuthBridge) {
        this.bridge = bridge
    }

    internal fun requireBridge(): IosAuthBridge = checkNotNull(bridge) {
        "Install the iOS auth bridge before creating MainViewController."
    }
}

class IosGoogleAuthDataSource(private val bridge: IosAuthBridge) : GoogleAuthDataSource {
    override val currentUser: AuthUser? get() = bridge.currentUser

    override suspend fun signIn(): AuthUser = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            bridge.signIn { user, failure ->
                if (continuation.isActive) {
                    when {
                        failure != null -> continuation.resumeWithException(failure.toException())
                        user != null -> continuation.resume(user)
                        else -> continuation.resumeWithException(
                            AuthException(AuthError.Unknown("Firebase returned no user."))
                        )
                    }
                }
            }
        }
    }

    override suspend fun signOut(): Unit = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            bridge.signOut { failure ->
                if (continuation.isActive) {
                    if (failure != null) continuation.resumeWithException(failure.toException())
                    else continuation.resume(Unit)
                }
            }
        }
    }
}

class IosSignInDataSource(private val bridge: IosAuthBridge) : SignInRemoteDataSource {
    override suspend fun save(userId: String, data: SignedInData): Unit = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            bridge.saveSignInMetadata(userId, data.toMap()) { failure ->
                if (continuation.isActive) {
                    if (failure != null) continuation.resumeWithException(IllegalStateException(failure.message))
                    else continuation.resume(Unit)
                }
            }
        }
    }
}

private fun IosAuthFailure.toException() = AuthException(
    when (kind) {
        IosAuthFailureKind.Cancelled -> AuthError.Cancelled
        IosAuthFailureKind.Network -> AuthError.Network
        IosAuthFailureKind.Unavailable -> AuthError.Unavailable
        IosAuthFailureKind.Unknown -> AuthError.Unknown(message)
    }
)
