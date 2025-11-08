package org.techascent.muslim.halalscanner.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_halal_certified
import apphub.composeapp.generated.resources.text_halal_certified_reason
import apphub.composeapp.generated.resources.text_halal_doubtful
import apphub.composeapp.generated.resources.text_halal_doubtful_reason
import apphub.composeapp.generated.resources.text_halal_possible
import apphub.composeapp.generated.resources.text_halal_possible_reason
import apphub.composeapp.generated.resources.text_not_halal
import apphub.composeapp.generated.resources.text_not_halal_reason
import apphub.composeapp.generated.resources.text_unknown_status
import apphub.composeapp.generated.resources.text_unknown_status_reason
import org.jetbrains.compose.resources.StringResource
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.mapper.HalalStatus

sealed interface HalalScannerUiState {
    data object Init : HalalScannerUiState
    data object Loading : HalalScannerUiState
    data class Success(val data: ProductUiState) : HalalScannerUiState
    data class Error(val message: String) : HalalScannerUiState

}

data class ProductUiState(
    val brands: String? = null,
    val labels: String? = null,
    val labelsTags: List<String>? = null,
    val ingredientsText: String? = null,
    val imageUrl: String? = null,
    val halalUiState: HalalUiState
)

data class HalalUiState(
    val status: HalalStatus,
    val halalStatusRes: StringResource,
    val reasonRes: StringResource,
)

fun ProductDto.toUiState() = ProductUiState(
    brands = brands,
    labels = labels,
    labelsTags = labelsTags,
    ingredientsText = ingredientsText,
    imageUrl = imageUrl,
    halalUiState = HalalUiState(
        status = halalResult.status,
        reasonRes = getReasonByStatus(status = halalResult.status),
        halalStatusRes = getTitleByStatus(status = halalResult.status),
    )
)

private fun getTitleByStatus(
    status: HalalStatus
): StringResource {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> Res.string.text_halal_certified
        HalalStatus.HALAL_POSSIBLE -> Res.string.text_halal_possible
        HalalStatus.HALAL_DOUBTFUL -> Res.string.text_halal_doubtful
        HalalStatus.NOT_HALAL -> Res.string.text_not_halal
        HalalStatus.UNKNOWN -> Res.string.text_unknown_status
    }
}

private fun getReasonByStatus(
    status: HalalStatus
): StringResource {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> Res.string.text_halal_certified_reason
        HalalStatus.HALAL_POSSIBLE -> Res.string.text_halal_possible_reason
        HalalStatus.HALAL_DOUBTFUL -> Res.string.text_halal_doubtful_reason
        HalalStatus.NOT_HALAL -> Res.string.text_not_halal_reason
        HalalStatus.UNKNOWN -> Res.string.text_unknown_status_reason
    }
}