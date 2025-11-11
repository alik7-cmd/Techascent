package org.techascent.muslim.utility

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.techascent.muslim.utility.state.UtilityUiState

class UtilityViewModel : ViewModel(){

    internal val uiState: MutableStateFlow<UtilityUiState> =
        MutableStateFlow(UtilityUiState())
}