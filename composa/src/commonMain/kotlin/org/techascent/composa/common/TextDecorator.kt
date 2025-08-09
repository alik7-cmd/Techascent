package org.techascent.composa.common

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Immutable
data class TextDecorator(
    val style: TextStyle = TextStyle.Default,
    val color: Color = Color.Unspecified,
    val maxLine: Int = 1,
)
