package org.techascent.aifinder.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** AI Finder's light and dark colors live here; other apps keep their own palettes. */
internal data class AiFinderColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val mint: Color,
    val illustrationMint: Color,
    val teal: Color,
    val onTeal: Color,
    val ink: Color,
    val muted: Color,
    val border: Color,
    val disabledSurface: Color,
    val disabledBorder: Color,
    val disabledContent: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val errorBorder: Color,
    val errorText: Color,
    val inversePrimary: Color,
    val creditGradientStart: Color,
    val creditSurface: Color,
) {
    fun toColorScheme(): ColorScheme {
        val defaults = if (isDark) darkColorScheme() else lightColorScheme()
        return defaults.copy(
            primary = teal,
            onPrimary = onTeal,
            primaryContainer = mint,
            onPrimaryContainer = ink,
            secondary = teal,
            onSecondary = onTeal,
            secondaryContainer = mint,
            onSecondaryContainer = ink,
            tertiary = teal,
            onTertiary = onTeal,
            tertiaryContainer = mint,
            onTertiaryContainer = ink,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = errorText,
            background = background,
            onBackground = ink,
            surface = surface,
            onSurface = ink,
            surfaceVariant = mint,
            onSurfaceVariant = muted,
            surfaceTint = teal,
            surfaceContainerLowest = surface,
            surfaceContainerLow = background,
            surfaceContainer = background,
            surfaceContainerHigh = mint,
            surfaceContainerHighest = mint,
            outline = muted,
            outlineVariant = border,
            inverseSurface = ink,
            inverseOnSurface = background,
            inversePrimary = inversePrimary,
        )
    }

    companion object {
        val Light = AiFinderColors(
            isDark = false,
            background = Color(0xFFF7FAF8),
            surface = Color.White,
            mint = Color(0xFFE8F6F2),
            illustrationMint = Color(0xFFDEF3EE),
            teal = Color(0xFF137E78),
            onTeal = Color.White,
            ink = Color(0xFF193437),
            muted = Color(0xFF729090),
            border = Color(0xFFDFECE8),
            disabledSurface = Color(0xFFF4F6F5),
            disabledBorder = Color(0xFFD8DEDC),
            disabledContent = Color(0xFF65716E),
            error = Color(0xFFC62828),
            onError = Color.White,
            errorContainer = Color(0xFFFDECEA),
            errorBorder = Color(0xFFF6C9C4),
            errorText = Color(0xFF5F1A16),
            inversePrimary = Color(0xFF8DD5CB),
            creditGradientStart = Color(0xFF174A48),
            creditSurface = Color(0xFF137E78),
        )

        val Dark = AiFinderColors(
            isDark = true,
            background = Color(0xFF101918),
            surface = Color(0xFF182321),
            mint = Color(0xFF243632),
            illustrationMint = Color(0xFF293F3A),
            teal = Color(0xFF72D5C7),
            onTeal = Color(0xFF003731),
            ink = Color(0xFFE2EFEB),
            muted = Color(0xFFA5BBB5),
            border = Color(0xFF3A504A),
            disabledSurface = Color(0xFF202A28),
            disabledBorder = Color(0xFF3E4B47),
            disabledContent = Color(0xFF899A94),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF5F201C),
            errorBorder = Color(0xFF8C3832),
            errorText = Color(0xFFFFDAD6),
            inversePrimary = Color(0xFF137E78),
            creditGradientStart = Color(0xFF102E2C),
            creditSurface = Color(0xFF174A48),
        )
    }
}

internal val LocalAiFinderColors = staticCompositionLocalOf { AiFinderColors.Light }

object AiFinderPalette {
    val Background: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.background
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.surface
    val Mint: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.mint
    val IllustrationMint: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.illustrationMint
    val Teal: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.teal
    val OnTeal: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.onTeal
    val Ink: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.ink
    val Muted: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.muted
    val Border: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.border
    val DisabledSurface: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.disabledSurface
    val DisabledBorder: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.disabledBorder
    val DisabledContent: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.disabledContent
    val Error: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.error
    val ErrorContainer: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.errorContainer
    val ErrorBorder: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.errorBorder
    val ErrorText: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.errorText
    val CreditGradientStart: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.creditGradientStart
    val CreditSurface: Color @Composable @ReadOnlyComposable get() = LocalAiFinderColors.current.creditSurface
}
