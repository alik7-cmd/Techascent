package org.techascent.shared.data.api.halalscanner

import org.techascent.shared.data.OpenFoodFactsResponse

interface HalalScannerApi {
    suspend fun fetchProductByBarcode(barcode: String): OpenFoodFactsResponse
}