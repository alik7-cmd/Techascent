package org.techascent.muslim.utility.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_halal
import apphub.composeapp.generated.resources.ic_mosque
import apphub.composeapp.generated.resources.ic_quibla
import apphub.composeapp.generated.resources.ic_tasbeeh
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_nearby_mosque
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
    val imageRes: DrawableResource,
    val tint: Color = Color.Unspecified,
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
        imageRes = Res.drawable.ic_halal,
        tint = Color.Green
    ),

    FeatureItem(
        titleRes = Res.string.title_nearby_mosque,
        imageRes = Res.drawable.ic_mosque
    )

)