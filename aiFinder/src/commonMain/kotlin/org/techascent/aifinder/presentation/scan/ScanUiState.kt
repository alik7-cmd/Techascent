package org.techascent.aifinder.presentation.scan

import org.techascent.aifinder.domain.subscription.ScanAccess

data class ScanContent(
    val previewPhoto: org.techascent.aifinder.domain.vision.CapturedPhoto? = null,
    val isCapturing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val access: ScanAccess? = null,
    val isLoadingBalance: Boolean = true,
    val balanceError: Boolean = false,
    val requiresPurchase: Boolean = false,
    val isPreparingPurchase: Boolean = false,
    val purchasePreparationError: Boolean = false,
)

sealed interface ScanUiState {
    data object Loading : ScanUiState
    data class Success(val data: ScanContent) : ScanUiState
    data class Error(val message: String) : ScanUiState
}
