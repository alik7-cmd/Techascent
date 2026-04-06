package org.techascent.muslim.halalscanner.state

import kotlinx.serialization.Serializable
import org.techascent.shared.data.mapper.FlaggedIngredient
import org.techascent.shared.data.mapper.HalalChecker
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

fun ProductUiState.toHistoryItem(
    barcode: String? = null,
    source: ScanSource = ScanSource.SCANNER,
): ScanHistoryItem {
    return ScanHistoryItem(
        id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
        barcode = barcode,
        brands = brands,
        labels = labels,
        labelsTags = labelsTags,
        ingredientsText = ingredientsText?.joinToString(", "),
        imageUrl = imageUrl,
        halalStatus = halalUiState.status,
        flaggedIngredients = flaggedIngredients,
        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
        source = source,
    )
}

fun ScanHistoryItem.toProductUiState(): ProductUiState {
    val ingredientsList = ingredientsText?.let {
        val parts = it.split(",")
        parts.chunked(3).map { chunk -> chunk.joinToString(",") }
    }
    // Use stored flagged ingredients, or re-compute from raw text for backward compat
    val resolved = flaggedIngredients.ifEmpty {
        ingredientsText?.let { HalalChecker.flagIngredients(it) } ?: emptyList()
    }
    return ProductUiState(
        brands = brands,
        labels = labels,
        labelsTags = labelsTags,
        ingredientsText = ingredientsList,
        imageUrl = imageUrl,
        halalUiState = HalalUiState(
            status = halalStatus,
            halalStatusRes = getTitleByStatus(halalStatus),
            reasonRes = getReasonByStatus(halalStatus),
        ),
        flaggedIngredients = resolved,
    )
}
