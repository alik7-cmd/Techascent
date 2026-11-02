package org.techascent.aifinder.domain.auth

interface AuthRepository {
    val currentUser: AuthUser?

    /** Fails with [AuthException] describing why sign-in didn't complete. */
    suspend fun signIn(provider: AuthProvider): Result<AuthUser>

    suspend fun signOut()
}
