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
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import kotlinx.coroutines.CancellationException
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.util.logDebug

class LoginViewModel(
    private val signIn: SignInUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val scanAccess: ScanAccessRepository,
    private val purchases: PurchaseGateway,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state = _state.asStateFlow()

    val isSignedIn: Boolean get() = getCurrentUser() != null

    fun onSignIn(provider: AuthProvider) {
        if (_state.value is LoginUiState.Loading) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            signIn(provider)
                .onSuccess { user ->
                    // Load what scanning needs, but never block login on it: the scan gate retries later.
                    bestEffort("Scan credits") { scanAccess.refresh() }
                    bestEffort("Purchases identity") {
                        if (purchases.prepare()) purchases.identify(user.id)
                    }
                    _state.value = LoginUiState.SignedIn
                }
                .onFailure { e ->
                    val error = (e as? AuthException)?.error ?: AuthError.Unknown(e.message)
                    _state.value =
                        error.toMessageRes()?.let(LoginUiState::Error) ?: LoginUiState.Idle
                }
        }
    }

    private suspend fun bestEffort(what: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logDebug("Login", "$what failed after sign-in: ${e.message}")
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
}
