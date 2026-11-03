package org.techascent.aifinder.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.auth.usecase.GetCurrentUserUseCase
import org.techascent.aifinder.domain.auth.usecase.SignOutUseCase
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.util.logDebug

class AccountViewModel(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val signOut: SignOutUseCase,
    private val purchases: PurchaseGateway,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val state = _state.asStateFlow()

    /** Reloads the signed-in user each time the screen opens, since accounts can change between visits. */
    fun load() {
        _state.value = getCurrentUser()?.let { AccountUiState.Success(it.toAccountProfile()) }
            ?: AccountUiState.SignedOut
    }

    fun requestLogout() = updateSuccess {
        if (it.isSigningOut) it else it.copy(showLogoutConfirmation = true, logoutFailed = false)
    }

    fun dismissLogout() = updateSuccess { it.copy(showLogoutConfirmation = false) }

    fun dismissLogoutError() = updateSuccess { it.copy(logoutFailed = false) }

    fun confirmLogout() {
        val current = _state.value as? AccountUiState.Success ?: return
        if (current.isSigningOut) return
        _state.value = current.copy(showLogoutConfirmation = false, isSigningOut = true)
        viewModelScope.launch {
            try {
                signOut()
                try {
                    purchases.reset()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logDebug("Account", "Purchases logout failed: ${e.message}")
                }
                _state.value = AccountUiState.SignedOut
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateSuccess { it.copy(isSigningOut = false, logoutFailed = true) }
            }
        }
    }

    private inline fun updateSuccess(transform: (AccountUiState.Success) -> AccountUiState.Success) =
        _state.update { if (it is AccountUiState.Success) transform(it) else it }
}
