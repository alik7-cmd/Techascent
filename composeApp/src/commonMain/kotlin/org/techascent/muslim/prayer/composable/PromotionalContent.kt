package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.lazy.LazyListScope
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_halal
import apphub.composeapp.generated.resources.ic_scan
import apphub.composeapp.generated.resources.text_halal_promotion
import apphub.composeapp.generated.resources.title_halal_scanner
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.DrawableData
import org.techascent.composa.featurecard.FeatureCard
import org.techascent.composa.theming.ComposaTheme

internal fun LazyListScope.featureCard(
    onClick: () -> Unit,
) {
    item {
        FeatureCard(
            icon = DrawableData(
                imageRes = Res.drawable.ic_halal,
                tint = ComposaTheme.color.iconAction
            ),
            text = stringResource(Res.string.text_halal_promotion),
            buttonText = stringResource(Res.string.title_halal_scanner),
            leftIcon = DrawableData(
                imageRes = Res.drawable.ic_scan,
                tint = ComposaTheme.color.iconAction
            ),
            onClick = onClick
        )
    }

}