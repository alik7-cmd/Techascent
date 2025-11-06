package org.techascent.muslim.halalscanner.state

data class HalalScannerUiState(
    val barcode : String = "",
    val loading: Boolean = true,
    val resultText: String? = null,
    val productName: String? = null
)