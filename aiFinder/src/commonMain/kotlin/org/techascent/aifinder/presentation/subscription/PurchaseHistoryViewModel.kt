package org.techascent.aifinder.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.aifinder.data.subscription.PurchaseHistoryRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.util.logDebug

sealed interface PurchaseHistoryState {
    data object Loading : PurchaseHistoryState
    data class Ready(val purchases: List<ScanPurchase>) : PurchaseHistoryState
    data object Error : PurchaseHistoryState
}

class PurchaseHistoryViewModel(private val repository: PurchaseHistoryRepository) : ViewModel() {
    private val _state = MutableStateFlow<PurchaseHistoryState>(PurchaseHistoryState.Loading)
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun load() {
        if (job?.isActive == true) return
        _state.value = PurchaseHistoryState.Loading
        job = viewModelScope.launch {
            try {
                _state.value = PurchaseHistoryState.Ready(repository.load())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logDebug("PurchaseHistory", "Loading purchase history failed: ${e.message}")
                _state.value = PurchaseHistoryState.Error
            }
        }
    }
}
