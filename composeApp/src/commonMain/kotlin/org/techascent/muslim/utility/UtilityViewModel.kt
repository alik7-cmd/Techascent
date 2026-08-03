package org.techascent.muslim.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.techascent.muslim.utility.state.UtilityUiState

class UtilityViewModel(
    private val usageRepository: FeatureUsageRepository,
) : ViewModel() {

    internal val uiState: MutableStateFlow<UtilityUiState> =
        MutableStateFlow(UtilityUiState())

    /**
     * Records that the user tapped [featureId] in the Explore tab.
     * Call this right before navigating so the count is persisted even if the user
     * immediately force-closes the app.
     */
    fun recordUsage(featureId: FeatureId) = viewModelScope.launch {
        usageRepository.recordUsage(featureId)
    }
}