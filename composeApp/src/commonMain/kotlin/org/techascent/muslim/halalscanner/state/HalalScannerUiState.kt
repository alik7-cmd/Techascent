package org.techascent.muslim.halalscanner.state

import dev.icerock.moko.permissions.PermissionState
import org.techascent.shared.data.mapper.HalalResult

data class HalalScannerUiState(
    val barcode : String = "",
    val loading: Boolean = true,
    val resultText: String? = null,
    val productName: String? = null,
    val cameraPermitted: PermissionState = PermissionState.NotDetermined,
    val shouldShowScanner: Boolean = true,
    val halalResult: HalalResult? = null
)