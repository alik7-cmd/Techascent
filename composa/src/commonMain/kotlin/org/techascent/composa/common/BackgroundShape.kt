package org.techascent.composa.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@Immutable
sealed interface BackgroundShape {

    @Stable
    @Immutable
    data object None : BackgroundShape

    @Stable
    @Immutable
    data class Circle(
        val color: Color,
        val isLarge: Boolean = true
    ) : BackgroundShape
}
