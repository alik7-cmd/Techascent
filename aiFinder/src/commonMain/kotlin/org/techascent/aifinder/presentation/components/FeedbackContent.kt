package org.techascent.aifinder.presentation.components

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.action_retry
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun LoadingContent() { CircularProgressIndicator(color = AiFinderPalette.Teal) }

@Composable
fun ErrorContent(message: String, retry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp12)) {
        Text(message, color = AiFinderPalette.Ink)
        PrimaryAction(stringResource(Res.string.action_retry), retry)
    }
}
