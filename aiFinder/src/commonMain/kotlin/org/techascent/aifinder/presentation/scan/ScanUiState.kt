package org.techascent.aifinder.presentation.scan

data class ScanContent(val selectedLocation: String = "Living room › TV cabinet › Drawer 2")

sealed interface ScanUiState {
    data object Loading : ScanUiState
    data class Success(val data: ScanContent) : ScanUiState
    data class Error(val message: String) : ScanUiState
}
