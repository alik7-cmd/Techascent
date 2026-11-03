package org.techascent.aifinder.presentation.login

import org.jetbrains.compose.resources.StringResource
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.util.Platform
import org.techascent.aifinder.util.getPlatform

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val errorRes: StringResource) : LoginUiState
    data object SignedIn: LoginUiState
}
