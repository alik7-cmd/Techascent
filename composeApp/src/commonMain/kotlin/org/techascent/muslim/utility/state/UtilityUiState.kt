package org.techascent.muslim.utility.state

import androidx.compose.runtime.Stable
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_quibla
import apphub.composeapp.generated.resources.ic_scan
import apphub.composeapp.generated.resources.ic_tasbeeh
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_tasbeeh
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class UtilityUiState(
    @Stable
    val listOfFeatures: List<FeatureItem> = featureList
)

data class FeatureItem(
    val titleRes: StringResource,
    val imageRes : DrawableResource
)

private val featureList = listOf(
    FeatureItem(
        titleRes = Res.string.title_tasbeeh,
        imageRes = Res.drawable.ic_tasbeeh
    ),
    FeatureItem(
        titleRes = Res.string.title_quibla,
        imageRes = Res.drawable.ic_quibla
    ),

    FeatureItem(
        titleRes = Res.string.title_halal_scanner,
        imageRes = Res.drawable.ic_scan
    )

)