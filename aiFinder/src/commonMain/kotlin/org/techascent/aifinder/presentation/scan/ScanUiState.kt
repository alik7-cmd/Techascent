package org.techascent.aifinder.presentation.scan

data class ScanContent(
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ScanUiState {
    data object Loading : ScanUiState
    data class Success(val data: ScanContent) : ScanUiState
    data class Error(val message: String) : ScanUiState
}
