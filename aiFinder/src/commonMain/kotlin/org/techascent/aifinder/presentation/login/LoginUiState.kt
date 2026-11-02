package org.techascent.aifinder.presentation.login

import org.jetbrains.compose.resources.StringResource
import org.techascent.aifinder.domain.auth.AuthProvider

data class LoginUiState(
    val loadingProvider: AuthProvider? = null,
    val errorRes: StringResource? = null,
    val isSignedIn: Boolean = false,
) {
    val isLoading: Boolean get() = loadingProvider != null
}
