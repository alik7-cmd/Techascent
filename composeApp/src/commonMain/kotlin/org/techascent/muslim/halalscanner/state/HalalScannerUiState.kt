package org.techascent.muslim.halalscanner.state

import dev.icerock.moko.permissions.PermissionState
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.mapper.HalalResult

/*data class HalalScannerUiState(
    val barcode : String = "",
    val loading: Boolean = false,
    val resultText: String? = null,
    val productName: String? = null,
    val cameraPermitted: PermissionState = PermissionState.NotDetermined,
    val shouldShowScanner: Boolean = true,
    val halalResult: HalalResult? = null
)*/

sealed interface HalalScannerUiState {
    data object Init : HalalScannerUiState
    data object Loading : HalalScannerUiState
    data class Success(val data: ProductDto): HalalScannerUiState
    data class Error(val message: String) : HalalScannerUiState

}