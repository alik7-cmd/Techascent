package org.techascent.aifinder.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.login_error_network
import apphub.aifinder.generated.resources.login_error_no_account
import apphub.aifinder.generated.resources.login_error_unavailable
import apphub.aifinder.generated.resources.login_error_unknown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.usecase.GetCurrentUserUseCase
import org.techascent.aifinder.domain.auth.usecase.SignInUseCase

class LoginViewModel(
    private val signIn: SignInUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    val isSignedIn: Boolean get() = getCurrentUser() != null

    fun onSignIn(provider: AuthProvider) {
        if (_state.value.isLoading) return
        _state.update { it.copy(loadingProvider = provider, errorRes = null) }
        viewModelScope.launch {
            signIn(provider)
                .onSuccess { _state.update { it.copy(loadingProvider = null, isSignedIn = true) } }
                .onFailure { e ->
                    val error = (e as? AuthException)?.error ?: AuthError.Unknown(e.message)
                    _state.update { it.copy(loadingProvider = null, errorRes = error.toMessageRes()) }
                }
        }
    }

    fun onSignedInHandled() = _state.update { it.copy(isSignedIn = false) }

    fun dismissError() = _state.update { it.copy(errorRes = null) }
}

/** Returns null for errors that shouldn't be surfaced, such as the user dismissing the picker. */
internal fun AuthError.toMessageRes(): StringResource? = when (this) {
    AuthError.Cancelled -> null
    AuthError.NoAccount -> Res.string.login_error_no_account
    AuthError.Network -> Res.string.login_error_network
    AuthError.Unavailable -> Res.string.login_error_unavailable
    is AuthError.Unknown -> Res.string.login_error_unknown
}
