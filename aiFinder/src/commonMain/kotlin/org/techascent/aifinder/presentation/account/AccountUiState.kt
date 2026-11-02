package org.techascent.aifinder.presentation.account

import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthUser

data class AccountProfile(
    val name: String?,
    val email: String?,
    val initial: String,
    val provider: AuthProvider?,
)

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data object SignedOut : AccountUiState
    data class Success(
        val profile: AccountProfile,
        val showLogoutConfirmation: Boolean = false,
        val isSigningOut: Boolean = false,
        val logoutFailed: Boolean = false,
    ) : AccountUiState
}

internal fun AuthUser.toAccountProfile(): AccountProfile {
    val name = displayName?.trim()?.takeIf { it.isNotEmpty() }
    val email = email?.trim()?.takeIf { it.isNotEmpty() }
    val initial = (name ?: email)?.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?"
    return AccountProfile(name = name, email = email, initial = initial, provider = provider)
}
