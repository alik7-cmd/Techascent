package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

/** Shared back chevron with a full-size touch target. */
@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBackIos,
            contentDescription = stringResource(Res.string.back),
            tint = AiFinderPalette.Ink,
            modifier = Modifier.size(20.dp)
        )
    }
}
