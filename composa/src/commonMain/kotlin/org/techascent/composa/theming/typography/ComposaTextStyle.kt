package org.techascent.composa.theming.typography

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

fun composaTextStyle(
    fontFamily: FontFamily? = null,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = TextUnit.Unspecified
): TextStyle {
    return TextStyle(
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        platformStyle = null
    )
}
