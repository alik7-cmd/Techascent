package org.techascent.shared.data.repository.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.network.ResultState

interface HalalScannerRepository {
    fun fetchProductByBarcode(barcode: String): Flow<ResultState<ProductDto>>
}