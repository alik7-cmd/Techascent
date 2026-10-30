package org.techascent.aifinder.presentation.scan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ScanViewModel() : ViewModel() {
    var state: ScanUiState by mutableStateOf(ScanUiState.Success(ScanContent()))
        private set
    fun retry() { state = ScanUiState.Success(ScanContent()) }
}
