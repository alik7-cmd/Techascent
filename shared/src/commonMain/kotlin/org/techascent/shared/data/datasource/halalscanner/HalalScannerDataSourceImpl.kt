package org.techascent.shared.data.datasource.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.OpenFoodFactsResponse
import org.techascent.shared.data.api.halalscanner.HalalScannerApi
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.network.ResultState
import org.techascent.shared.network.baseRemoteCall

class HalalScannerDataSourceImpl(
    private val api: HalalScannerApi,
) : HalalScannerDataSource {
    override fun fetchProductByBarcode(
        barcode: String,
        onMapData: (OpenFoodFactsResponse) -> ProductDto
    ): Flow<ResultState<ProductDto>> {
        return baseRemoteCall(
            onCallRemoteApi = {
                api.fetchProductByBarcode(barcode)
            },
            onMapData = onMapData
        )
    }
}