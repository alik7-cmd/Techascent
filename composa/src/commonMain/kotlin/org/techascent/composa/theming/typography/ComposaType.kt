package org.techascent.composa.theming.typography

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import apphub.composa.generated.resources.Res
import apphub.composa.generated.resources.maisonneue_book
import org.jetbrains.compose.resources.Font

@Composable
internal fun ComposaFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.maisonneue_book,
        style = FontStyle.Normal,
        weight = FontWeight.W400,
    )
)

// Roboto is Android's system font and iOS ships it natively — no need to bundle it.
// Using FontFamily.Default lets the platform render body text with its system Roboto,
// saving ~340 KB from the app bundle (roboto_regular.ttf + roboto_italic.ttf removed).
private val RobotoFontFamily: FontFamily = FontFamily.Default

private val defaultMaterialTypography = Typography()

@Composable
internal fun M3ComposaTypography() = Typography(
    displayLarge = defaultMaterialTypography.displayLarge.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    displayMedium = defaultMaterialTypography.displayMedium.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    displaySmall = defaultMaterialTypography.displaySmall.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    headlineLarge = defaultMaterialTypography.headlineLarge.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    headlineMedium = defaultMaterialTypography.headlineMedium.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    headlineSmall = defaultMaterialTypography.headlineSmall.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    titleLarge = defaultMaterialTypography.titleLarge.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Normal),
    titleMedium = defaultMaterialTypography.titleMedium.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Medium),
    titleSmall = defaultMaterialTypography.titleSmall.copy(fontFamily = ComposaFontFamily(), fontWeight = FontWeight.Medium),
    bodyLarge = defaultMaterialTypography.bodyLarge.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Normal),
    bodyMedium = defaultMaterialTypography.bodyMedium.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Medium),
    bodySmall = defaultMaterialTypography.bodySmall.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Normal),
    labelLarge = defaultMaterialTypography.labelLarge.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = defaultMaterialTypography.labelMedium.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = defaultMaterialTypography.labelSmall.copy(fontFamily = RobotoFontFamily, fontWeight = FontWeight.Medium),
)
