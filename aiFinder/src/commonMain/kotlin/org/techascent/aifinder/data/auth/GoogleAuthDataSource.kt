package org.techascent.aifinder.data.auth

import org.techascent.aifinder.domain.auth.AuthUser

/** Platform Google sign-in. Implementations throw [org.techascent.aifinder.domain.auth.AuthException]. */
interface GoogleAuthDataSource {
    val currentUser: AuthUser?
    suspend fun signIn(): AuthUser
    suspend fun signOut()
}
