package org.techascent.composa.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import org.techascent.composa.theming.color.ComposaColor
import org.techascent.composa.theming.color.DarkColors
import org.techascent.composa.theming.color.LightColors
import org.techascent.composa.theming.color.composaDarkTheme
import org.techascent.composa.theming.color.composaLightTheme
import org.techascent.composa.theming.shape.ComposaMaterial3Shape
import org.techascent.composa.theming.shape.ComposaShape
import org.techascent.composa.theming.typography.ComposaTypography
import org.techascent.composa.theming.typography.M3ComposaTypography
import org.techascent.composa.theming.typography.composaTypography

@Composable
fun ComposaTheme(
    isNightMode: Boolean = isSystemInDarkTheme(),
    shape: ComposaShape = ComposaShape(),
    m3Shape: ComposaMaterial3Shape = ComposaMaterial3Shape(),
    setStatusBarColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    ProvideComposaColors(isNightMode = isNightMode) {
        /*if (setStatusBarColor) {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(ComposaTheme.color.backgroundAppBackground)
        }*/

        val colors = if (!isNightMode) {
            LightColors
        } else {
            DarkColors
        }

        MaterialTheme(
            typography = provideM3Typography(),
            colorScheme = colors
        ) {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalComposaShape provides shape,
                    LocalComposaM3Shape provides m3Shape,
                    LocalComposaTypography provides composaTypography(),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun ProvideComposaColors(
    isNightMode: Boolean,
    content: @Composable () -> Unit,
) {

    val colors = getTokenColorPalette(
        isNightMode = isNightMode
    )

    CompositionLocalProvider(
        LocalComposaTokenColors provides colors,
        content = content
    )
}

@Composable
private fun provideM3Typography(): Typography {
    return M3ComposaTypography()
}

private fun getTokenColorPalette(isNightMode: Boolean): ComposaColor {
    if (isNightMode) return composaDarkTheme
    return composaLightTheme
}

object ComposaTheme {
    val color: ComposaColor
        @Composable
        @ReadOnlyComposable
        get() = LocalComposaTokenColors.current

    val shape: ComposaShape
        @Composable
        @ReadOnlyComposable
        get() = LocalComposaShape.current

    val shapeM3: ComposaMaterial3Shape
        @Composable
        @ReadOnlyComposable
        get() = LocalComposaM3Shape.current

    val typography: ComposaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalComposaTypography.current
}

val LocalComposaTokenColors: ProvidableCompositionLocal<ComposaColor> =
    staticCompositionLocalOf {
        error("Composa colors are not provided.")
    }

val LocalComposaShape: ProvidableCompositionLocal<ComposaShape> = staticCompositionLocalOf {
    error("No composa shape defined")
}

val LocalComposaM3Shape: ProvidableCompositionLocal<ComposaMaterial3Shape> =
    staticCompositionLocalOf {
        error("No composa m3 shape defined")
    }

val LocalComposaTypography: ProvidableCompositionLocal<ComposaTypography> =
    staticCompositionLocalOf { error("No composa typography defined") }
