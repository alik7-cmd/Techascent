package org.techascent.shared.data.repository.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.Product
import org.techascent.shared.data.datasource.halalscanner.HalalScannerDataSource
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.mapper.HalalChecker
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

    override fun checkIngredients(ingredientsText: String): ProductDto {
        val product = Product(
            productName = null,
            brands = null,
            labels = null,
            labelsTags = null,
            ingredients_text = ingredientsText,
            image_url = null,
            certificationTag = null,
        )
        val halalResult = HalalChecker.assessHalalStatus(product)
        return ProductDto(
            brands = null,
            labels = null,
            labelsTags = null,
            ingredientsText = ingredientsText,
            imageUrl = null,
            halalResult = halalResult,
        )
    }
}