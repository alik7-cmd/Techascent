package org.techascent.shared.data.repository.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.datasource.halalscanner.HalalScannerDataSource
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.mapper.toDto
import org.techascent.shared.network.ResultState

class HalalScannerRepositoryImpl(
    private val dataSource: HalalScannerDataSource
) : HalalScannerRepository {
    override fun fetchProductByBarcode(barcode: String): Flow<ResultState<ProductDto>> {
        return dataSource.fetchProductByBarcode(
            barcode = barcode,
            onMapData = { response ->
                response.toDto()
            }
        )
    }
}