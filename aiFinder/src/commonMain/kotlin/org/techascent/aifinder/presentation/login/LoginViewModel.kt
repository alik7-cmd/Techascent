package org.techascent.aifinder.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.login_error_network
import apphub.aifinder.generated.resources.login_error_no_account
import apphub.aifinder.generated.resources.login_error_unavailable
import apphub.aifinder.generated.resources.login_error_unknown
import apphub.aifinder.generated.resources.login_error_save_details
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
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import kotlinx.coroutines.CancellationException
import apphub.aifinder.generated.resources.login_error_scan_access

class LoginViewModel(
    private val signIn: SignInUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val scanAccess: ScanAccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state = _state.asStateFlow()

    val isSignedIn: Boolean get() = getCurrentUser() != null

    fun onSignIn(provider: AuthProvider) {
        if (_state.value is LoginUiState.Loading) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            signIn(provider)
                .onSuccess {
                    try {
                        scanAccess.refresh()
                        _state.value = LoginUiState.SignedIn
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _state.value = LoginUiState.Error(Res.string.login_error_scan_access)
                    }
                }
                .onFailure { e ->
                    val error = (e as? AuthException)?.error ?: AuthError.Unknown(e.message)
                    _state.value =
                        error.toMessageRes()?.let(LoginUiState::Error) ?: LoginUiState.Idle
                }
        }
    }

    fun onSignedInHandled() = _state.update {
        if (it is LoginUiState.SignedIn) LoginUiState.Idle else it
    }

    fun dismissError() = _state.update {
        if (it is LoginUiState.Error) LoginUiState.Idle else it
    }
}

/** Returns null for errors that shouldn't be surfaced, such as the user dismissing the picker. */
internal fun AuthError.toMessageRes(): StringResource? = when (this) {
    is AuthError.NoAccount -> Res.string.login_error_no_account
    is AuthError.Network -> Res.string.login_error_network
    is AuthError.Unavailable -> Res.string.login_error_unavailable
    is AuthError.Unknown -> Res.string.login_error_unknown
    is AuthError.Cancelled -> null
    is AuthError.SignInDataSaveFailed -> Res.string.login_error_save_details
}
