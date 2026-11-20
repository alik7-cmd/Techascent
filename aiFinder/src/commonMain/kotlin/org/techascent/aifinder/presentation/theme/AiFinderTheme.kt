package org.techascent.aifinder.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.techascent.composa.theming.ComposaTheme

@Composable
fun AiFinderTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) AiFinderColors.Dark else AiFinderColors.Light
    ComposaTheme(isNightMode = colors.isDark) {
        CompositionLocalProvider(LocalAiFinderColors provides colors) {
            MaterialTheme(colorScheme = colors.toColorScheme()) {
                CompositionLocalProvider(
                    LocalTextSelectionColors provides TextSelectionColors(
                        handleColor = colors.teal,
                        backgroundColor = colors.teal.copy(alpha = 0.25f),
                    ),
                    content = content,
                )
            }
        }
    }
}
