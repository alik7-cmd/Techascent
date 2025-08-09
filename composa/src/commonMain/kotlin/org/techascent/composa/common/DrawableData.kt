package org.techascent.composa.common

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

data class DrawableData(
    val imageRes: DrawableResource,
    val tint: Color,
    val contentDescription: String? = null
)
