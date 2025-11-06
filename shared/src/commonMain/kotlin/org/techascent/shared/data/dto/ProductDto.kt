package org.techascent.shared.data.dto

data class ProductDto(
    val brands: String? = null,
    val labels: String? = null,
    val labelsTags: List<String>? = null,
    val ingredientsText: String? = null,
    val imageUrl: String? = null,
    val isHalal : Boolean
)