package org.techascent.muslim.tasbeeh

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.techascent.muslim.tasbeeh.state.TasbeehUiState

class TasbeehViewModel() : ViewModel() {

    internal val uiState: MutableStateFlow<TasbeehUiState> =
        MutableStateFlow(TasbeehUiState())

    fun onUpdateDialogVisibility() {
        uiState.update { currentState ->
            currentState.copy(shouldShowResetDialog = !currentState.shouldShowResetDialog)
        }
    }

    fun onProceedClick() {
        uiState.update { currentState ->
            currentState.copy(
                shouldShowResetDialog = false,
                count = 0
            )
        }
    }
}