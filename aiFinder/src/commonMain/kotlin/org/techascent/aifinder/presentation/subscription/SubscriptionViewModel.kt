package org.techascent.aifinder.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository

sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Ready(val access: ScanAccess) : SubscriptionUiState
    data object Error : SubscriptionUiState
}

class SubscriptionViewModel(private val repository: ScanAccessRepository) : ViewModel() {
    private val _state = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun refresh(onAllowed: (() -> Unit)? = null) {
        if (job?.isActive == true) return
        _state.value = SubscriptionUiState.Loading
        job = viewModelScope.launch {
            try {
                val access = repository.refresh()
                _state.value = SubscriptionUiState.Ready(access)
                if (access.canScan) onAllowed?.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = SubscriptionUiState.Error
            }
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        _state.value = SubscriptionUiState.Loading
    }
}
