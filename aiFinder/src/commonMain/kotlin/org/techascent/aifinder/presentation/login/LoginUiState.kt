package org.techascent.aifinder.presentation.login

import org.jetbrains.compose.resources.StringResource
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.util.Platform
import org.techascent.aifinder.util.getPlatform

data class LoginUiState(
    val loadingProvider: AuthProvider? = null,
    val errorRes: StringResource? = null,
    val isSignedIn: Boolean = false,
    val platform: Platform = getPlatform()
) {
    val isLoading: Boolean get() = loadingProvider != null
}
