package org.techascent.aifinder.presentation.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import org.techascent.composa.theming.ComposaTheme

private val AiFinderColors = lightColorScheme(
    primary = AiFinderPalette.Teal,
    onPrimary = Color.White,
    primaryContainer = AiFinderPalette.Mint,
    onPrimaryContainer = AiFinderPalette.Ink,
    secondary = AiFinderPalette.Teal,
    onSecondary = Color.White,
    secondaryContainer = AiFinderPalette.Mint,
    onSecondaryContainer = AiFinderPalette.Ink,
    tertiary = AiFinderPalette.Teal,
    onTertiary = Color.White,
    tertiaryContainer = AiFinderPalette.Mint,
    onTertiaryContainer = AiFinderPalette.Ink,
    background = AiFinderPalette.Background,
    onBackground = AiFinderPalette.Ink,
    surface = AiFinderPalette.Surface,
    onSurface = AiFinderPalette.Ink,
    surfaceVariant = AiFinderPalette.Mint,
    onSurfaceVariant = Color(0xFF496563),
    surfaceTint = AiFinderPalette.Teal,
    surfaceContainerLowest = AiFinderPalette.Surface,
    surfaceContainerLow = AiFinderPalette.Background,
    surfaceContainer = AiFinderPalette.Background,
    surfaceContainerHigh = AiFinderPalette.Mint,
    surfaceContainerHighest = AiFinderPalette.Mint,
    outline = Color(0xFF72908A),
    outlineVariant = AiFinderPalette.Border,
    inverseSurface = AiFinderPalette.Ink,
    inverseOnSurface = AiFinderPalette.Background,
    inversePrimary = Color(0xFF8DD5CB),
)

@Composable
fun AiFinderTheme(content: @Composable () -> Unit) {
    ComposaTheme(isNightMode = false) {
        MaterialTheme(colorScheme = AiFinderColors) {
            CompositionLocalProvider(
                LocalTextSelectionColors provides TextSelectionColors(
                    handleColor = AiFinderPalette.Teal,
                    backgroundColor = AiFinderPalette.Teal.copy(alpha = 0.25f),
                ),
                content = content,
            )
        }
    }
}
