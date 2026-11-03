package org.techascent.aifinder.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.util.getDeviceName
import org.techascent.aifinder.util.getPlatform
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class AuthRepositoryImpl(
    private val google: GoogleAuthDataSource,
    private val signInRemote: SignInRemoteDataSource,
    private val deviceName: () -> String = ::getDeviceName,
) : AuthRepository {
    override val currentUser: AuthUser? get() = google.currentUser

    override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = try {
        val user = when (provider) {
            AuthProvider.Google -> google.signIn()
            AuthProvider.Apple -> throw AuthException(AuthError.Unavailable)
        }
        try {
            withTimeoutOrNull(15_000L.milliseconds) {
                signInRemote.save(
                    user.id, SignedInData(
                        platform = getPlatform(),
                        provider = provider,
                        deviceName = deviceName(),
                        signedInAt = currentTimeMillis()
                    )
                )
                true
            } ?: error("Saving sign-in details timed out.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthException(AuthError.SignInDataSaveFailed, e)
        }
        Result.success(user)
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthException(AuthError.Unknown(e.message), e))
    }

    override suspend fun signOut() = google.signOut()
}

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
