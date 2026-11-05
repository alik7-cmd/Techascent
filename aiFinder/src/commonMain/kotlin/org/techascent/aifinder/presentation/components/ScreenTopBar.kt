package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ScreenTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = AiFinderPalette.Background, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            ).heightIn(min = 64.dp).padding(start = 12.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackButton(onBack)
            Text(
                text = title,
                modifier = Modifier.weight(1f).semantics { heading() },
                color = AiFinderPalette.Ink,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
