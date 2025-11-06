package org.techascent.shared.data.api.halalscanner

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.techascent.shared.data.OpenFoodFactsResponse


class HalalScannerApiImpl(private val client: HttpClient) : HalalScannerApi {
    override suspend fun fetchProductByBarcode(barcode: String): OpenFoodFactsResponse {
        return client.get("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
            .body()
    }
}