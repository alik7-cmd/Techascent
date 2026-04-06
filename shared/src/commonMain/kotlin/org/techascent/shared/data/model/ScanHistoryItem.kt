package org.techascent.shared.data.model

import kotlinx.serialization.Serializable
import org.techascent.shared.data.mapper.FlaggedIngredient
import org.techascent.shared.data.mapper.HalalStatus

@Serializable
data class ScanHistoryItem(
    val id: String, // UUID-like unique key
    val barcode: String? = null,
    val brands: String? = null,
    val labels: String? = null,
    val labelsTags: List<String>? = null,
    val ingredientsText: String? = null,
    val imageUrl: String? = null,
    val halalStatus: HalalStatus,
    val flaggedIngredients: List<FlaggedIngredient> = emptyList(),
    val timestamp: Long, // epoch millis
    val source: ScanSource = ScanSource.SCANNER,
)

@Serializable
enum class ScanSource {
    SCANNER,
    MANUAL_BARCODE,
    MANUAL_INGREDIENTS,
}

