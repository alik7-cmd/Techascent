package org.techascent.aifinder.domain.auth

data class AuthUser(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val provider: AuthProvider? = null,
)

enum class AuthProvider { Google, Apple }

sealed interface AuthError {
    /** The user closed the account picker; not shown as an error. */
    data object Cancelled : AuthError
    data object NoAccount : AuthError
    data object Network : AuthError
    data object Unavailable : AuthError
    data class Unknown(val message: String?) : AuthError
}

class AuthException(val error: AuthError, cause: Throwable? = null) :
    Exception(error.toString(), cause)
