package org.techascent.muslim.halalscanner.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_product_not_found
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

@Composable
internal fun ErrorContent(
    onNavigateBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Medium)
        ) {
            Text(
                text = stringResource(Res.string.text_product_not_found),
                style = ComposaTheme.typography.caption
            )

            ComposaButton(
                text = stringResource(Res.string.text_cancel),
                onClick = onNavigateBack,
                iconTint = Color.Unspecified,
            )
        }

    }

}