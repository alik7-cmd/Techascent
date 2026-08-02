package org.techascent.muslim.halalscanner.state

import org.techascent.shared.data.mapper.HalalChecker
import org.techascent.shared.data.model.ScanHistoryItem
import org.techascent.shared.data.model.ScanSource

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
        it.split(",").map { part -> part.trim() }.filter { part -> part.isNotEmpty() }
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
