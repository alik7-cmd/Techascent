package org.techascent.shared.data.datasource.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.OpenFoodFactsResponse
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.network.ResultState

interface HalalScannerDataSource {
    fun fetchProductByBarcode(
        barcode: String,
        onMapData: (OpenFoodFactsResponse) -> ProductDto
    ): Flow<ResultState<ProductDto>>
}