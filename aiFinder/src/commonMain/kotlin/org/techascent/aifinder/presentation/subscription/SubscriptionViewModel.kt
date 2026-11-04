package org.techascent.aifinder.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.domain.subscription.StorePurchase
import org.techascent.aifinder.util.logDebug
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState

    /**
     * @param purchasesEnabled true when the store paywall can sell packs to this user.
     * @param creditsPending true when a purchase finished but the backend hasn't credited it yet.
     */
    data class Ready(
        val access: ScanAccess,
        val purchasesEnabled: Boolean = false,
        val creditsPending: Boolean = false,
    ) : SubscriptionUiState

    /** A purchase completed and we're waiting for the backend to add the scans. */
    data object AddingScans : SubscriptionUiState
    data object Error : SubscriptionUiState
}

class SubscriptionViewModel(
    private val repository: ScanAccessRepository,
    private val purchases: PurchaseGateway,
    private val creditPollAttempts: Int = 8,
    private val creditPollInterval: Duration = 2.seconds,
) : ViewModel() {
    private val _state = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun refresh(onAllowed: (() -> Unit)? = null) {
        if (job?.isActive == true) return
        _state.value = SubscriptionUiState.Loading
        job = viewModelScope.launch {
            try {
                val access = repository.refresh()
                if (access.canScan && onAllowed != null) {
                    _state.value = SubscriptionUiState.Ready(access)
                    onAllowed()
                } else {
                    _state.value = SubscriptionUiState.Ready(access, purchasesEnabled = preparePurchases(access))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = SubscriptionUiState.Error
            }
        }
    }

    /**
     * Saves the transaction to the purchase history and credits the wallet. If that write fails,
     * keeps polling in case the purchase is credited elsewhere (e.g. by a backend webhook).
     */
    fun onPurchaseCompleted(purchase: StorePurchase? = null, onAllowed: (() -> Unit)? = null) {
        val previous = (_state.value as? SubscriptionUiState.Ready)?.access?.remainingScans ?: 0L
        job?.cancel()
        _state.value = SubscriptionUiState.AddingScans
        job = viewModelScope.launch {
            val record = purchase?.let(ScanPurchase::from)
            if (record == null) {
                logDebug(TAG, "Purchase has no known scan pack: ${purchase?.productIds}")
            } else {
                try {
                    val access = repository.recordPurchase(record)
                    _state.value = SubscriptionUiState.Ready(access, purchasesEnabled = true)
                    if (access.canScan) onAllowed?.invoke()
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logDebug(TAG, "Recording purchase ${record.purchaseId} failed: ${e.message}")
                }
            }
            var latest: ScanAccess? = null
            repeat(creditPollAttempts) { attempt ->
                if (attempt > 0) delay(creditPollInterval)
                try {
                    val access = repository.refresh()
                    latest = access
                    if (access.remainingScans > previous) {
                        _state.value = SubscriptionUiState.Ready(access, purchasesEnabled = true)
                        if (access.canScan) onAllowed?.invoke()
                        return@launch
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logDebug(TAG, "Wallet refresh after purchase failed: ${e.message}")
                }
            }
            _state.value = latest?.let {
                SubscriptionUiState.Ready(it, purchasesEnabled = true, creditsPending = true)
            } ?: SubscriptionUiState.Error
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        _state.value = SubscriptionUiState.Loading
    }

    private suspend fun preparePurchases(access: ScanAccess): Boolean = try {
        purchases.prepare().also { if (it) purchases.identify(access.userId) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Never sell packs under an anonymous store id: the backend couldn't credit the wallet.
        logDebug(TAG, "Purchases unavailable: ${e.message}")
        false
    }

    private companion object {
        const val TAG = "Subscription"
    }
}
