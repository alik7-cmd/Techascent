package org.techascent.aifinder.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun SectionTitle(text: String, fontSize: TextUnit = 24.sp, lineHeight: TextUnit = TextUnit.Unspecified) {
    Text(text, fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = FontWeight.Bold,
        color = AiFinderPalette.Ink
    )
}
