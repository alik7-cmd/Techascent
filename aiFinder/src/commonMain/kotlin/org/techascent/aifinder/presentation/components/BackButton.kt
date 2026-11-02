package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.offset
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

/** Shared top-bar back chevron; offset so the glyph lines up with the screen's content edge. */
@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier.offset(x = (-12).dp)) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBackIos,
            contentDescription = stringResource(Res.string.back),
            tint = AiFinderPalette.Ink,
            modifier = Modifier.size(20.dp)
        )
    }
}
