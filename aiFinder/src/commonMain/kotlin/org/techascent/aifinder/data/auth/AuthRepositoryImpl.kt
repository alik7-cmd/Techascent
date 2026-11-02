package org.techascent.aifinder.data.auth

import kotlinx.coroutines.CancellationException
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser

class AuthRepositoryImpl(private val google: GoogleAuthDataSource) : AuthRepository {
    override val currentUser: AuthUser? get() = google.currentUser

    override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = try {
        when (provider) {
            AuthProvider.Google -> Result.success(google.signIn())
            AuthProvider.Apple -> throw AuthException(AuthError.Unavailable)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthException(AuthError.Unknown(e.message), e))
    }

    override suspend fun signOut() = google.signOut()
}
