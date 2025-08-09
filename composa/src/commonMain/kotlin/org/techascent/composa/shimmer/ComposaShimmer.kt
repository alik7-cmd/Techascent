package org.techascent.composa.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

@Composable
fun Modifier.shimmerEffect(
    visible: Boolean = true,
    colorOverride: Color? = null,
    highLightOverride: Color? = null

): Modifier = composed {
    val color = colorOverride ?: Color.Gray
    val highLightColor = highLightOverride ?: ComposaTheme.color.backgroundNeutral
    this then Modifier.placeHolder(
        visible = visible,
        color = color,
        shape = ComposaTheme.shape.medium,
        highlight = highLightColor

    )
}

private fun Modifier.placeHolder(
    visible: Boolean = true,
    color: Color = Color.LightGray,
    shape: Shape = RoundedCornerShape(4.dp),
    highlight: Color = Color.White,
    shimmerWidth: Dp = 100.dp
): Modifier = composed {
    if (!visible) return@composed this

    val shimmerWidthPx = with(LocalDensity.current) { shimmerWidth.toPx() }

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -shimmerWidthPx,
        targetValue = shimmerWidthPx * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(color, highlight, color),
        start = Offset(shimmerX - shimmerWidthPx, 0f),
        end = Offset(shimmerX, 0f)
    )

    this
        .clip(shape)
        .drawWithContent {
            drawRect(shimmerBrush, size = Size(size.width, size.height))
        }
}

@Composable
fun CellShimmer() {
    Row(
        modifier = Modifier
            .background(ComposaTheme.color.backgroundAppBackground)
            .padding(horizontal = ComposaSpacing.Medium),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        Box(
            modifier = Modifier
                .align(CenterVertically)
                .size(ComposaSpacing.ExtraLarge)
                .shimmerEffect(visible = true, colorOverride = null, highLightOverride = null)
        )

        Column(
            modifier = Modifier
                .padding(ComposaSpacing.Medium)
                .weight(1f),
            verticalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
        ) {
            Text(
                text = "Text",
                modifier = Modifier
                    .size(height = 24.dp, width = 100.dp)
                    .shimmerEffect(true)
            )

            Text(
                text = "Text",
                modifier = Modifier
                    .size(height = 24.dp, width = 180.dp)
                    .shimmerEffect(true)
            )

        }

    }
}