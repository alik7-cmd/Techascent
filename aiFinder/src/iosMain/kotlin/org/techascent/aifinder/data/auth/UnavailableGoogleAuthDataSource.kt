package org.techascent.aifinder.data.auth

import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthUser

// iOS needs GoogleService-Info.plist and the Firebase/GoogleSignIn iOS SDKs before this can be real.
class UnavailableGoogleAuthDataSource : GoogleAuthDataSource {
    override val currentUser: AuthUser? = null
    override suspend fun signIn(): AuthUser = throw AuthException(AuthError.Unavailable)
    override suspend fun signOut() = Unit
}
