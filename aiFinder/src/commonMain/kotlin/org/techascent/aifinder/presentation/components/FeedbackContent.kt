package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun LoadingContent() { CircularProgressIndicator(color = AiFinderPalette.Teal) }

@Composable
fun ErrorContent(message: String, retry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = AiFinderPalette.Ink)
        PrimaryAction("Retry", retry)
    }
}
