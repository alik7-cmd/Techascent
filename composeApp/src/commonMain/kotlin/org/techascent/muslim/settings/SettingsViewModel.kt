package org.techascent.muslim.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.muslim.settings.state.getSettingsUiState

class SettingsViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(getSettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState


}