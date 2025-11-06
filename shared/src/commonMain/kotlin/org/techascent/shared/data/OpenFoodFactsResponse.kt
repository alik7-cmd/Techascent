package org.techascent.shared.data

import kotlinx.serialization.SerialName

data class OpenFoodFactsResponse(
    val status: Int,
    @SerialName("status_verbose") val statusVerbose: String? = null,
    val product: Product? = null
)

data class Product(
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    val labels: String? = null,
    @SerialName("labels_tags") val labelsTags: List<String>? = null,
    val ingredients_text: String? = null,
    val image_url: String? = null
)