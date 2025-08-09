package org.techascent.composa.theming.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class ComposaTypography(
    val title: TextStyle,
    val titleDemi: TextStyle,
    val titleEmphasized: TextStyle,
    val titleMedium: TextStyle,
    val titleMediumDemi: TextStyle,
    val titleMediumEmphasized: TextStyle,
    val titleLarge: TextStyle,
    val titleLargeDemi: TextStyle,
    val body: TextStyle,
    val bodyEmphasized: TextStyle,
    val subhead: TextStyle,
    val subheadEmphasized: TextStyle,
    val footnote: TextStyle,
    val footnoteEmphasized: TextStyle,
    val caption: TextStyle,
    val captionEmphasized: TextStyle,
    val buttonLarge: TextStyle,
    val buttonLargeEmphasized: TextStyle,
    val buttonSmall: TextStyle,
    val buttonSmallEmphasized: TextStyle
)

@Composable
internal fun composaTypography(): ComposaTypography{
    return ComposaTypography(
        title = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 28.sp
        ),
        titleDemi = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 28.sp
        ),
        titleEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        ),
        titleMedium = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 32.sp
        ),
        titleMediumDemi = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 32.sp
        ),
        titleMediumEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        ),
        titleLarge = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 44.sp
        ),
        titleLargeDemi = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 44.sp
        ),
        body = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        ),
        bodyEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp
        ),
        subhead = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        ),
        subheadEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        ),
        footnote = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp
        ),
        footnoteEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp
        ),
        caption = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp
        ),
        captionEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp
        ),
        buttonLarge = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        buttonLargeEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        buttonSmall = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
        buttonSmallEmphasized = composaTextStyle(
            fontFamily = ComposaFontFamily(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        )
    )
}
