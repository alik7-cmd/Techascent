package org.techascent.composa.theming.typography

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import apphub.composa.generated.resources.Res
import apphub.composa.generated.resources.maisonneue_book
import apphub.composa.generated.resources.roboto_italic
import apphub.composa.generated.resources.roboto_regular
import org.jetbrains.compose.resources.Font

@Composable
internal fun ComposaFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.maisonneue_book,
        style = FontStyle.Normal,
        weight = FontWeight.W400
    )
)

@Composable
internal fun RobotoRegularFontFamily() = FontFamily(
    Font(
        resource = Res.font.roboto_regular,
        style = FontStyle.Normal,
        weight = FontWeight.W400
    ),
    Font(
        resource = Res.font.roboto_italic,
        style = FontStyle.Italic,
        weight = FontWeight.W400
    ),
)

private val defaultMaterialTypography = Typography()

@Composable
internal fun M3ComposaTypography() = Typography(
    displayLarge = defaultMaterialTypography.displayLarge.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal
    ),
    displayMedium = defaultMaterialTypography.displayMedium.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal
    ),
    displaySmall = defaultMaterialTypography.displaySmall.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = defaultMaterialTypography.headlineLarge.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal,
    ),
    headlineMedium = defaultMaterialTypography.headlineMedium.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = defaultMaterialTypography.headlineSmall.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal
    ),
    titleLarge = defaultMaterialTypography.titleLarge.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Normal,
    ),
    titleMedium = defaultMaterialTypography.titleMedium.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Medium,
    ),
    titleSmall = defaultMaterialTypography.titleSmall.copy(
        fontFamily = ComposaFontFamily(),
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = defaultMaterialTypography.bodyLarge.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = defaultMaterialTypography.bodyMedium.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Medium
    ),
    bodySmall = defaultMaterialTypography.bodySmall.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = defaultMaterialTypography.labelLarge.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = defaultMaterialTypography.labelMedium.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = defaultMaterialTypography.labelSmall.copy(
        fontFamily = RobotoRegularFontFamily(),
        fontWeight = FontWeight.Medium,
    ),
)
