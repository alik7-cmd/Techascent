package org.techascent.muslim.method

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.techascent.muslim.method.state.MethodUiState

class MethodViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MethodUiState())
    val uiState: StateFlow<MethodUiState> = _uiState
}