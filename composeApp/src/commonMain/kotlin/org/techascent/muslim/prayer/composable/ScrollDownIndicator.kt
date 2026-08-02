package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing

@Composable
internal fun ScrollDownIndicator(
    alpha: Float,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(bottom = bottomPadding + 16.dp)
            .size(25.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(color = Color.Black.copy(alpha = 0.25f), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ComposaSpacing.Medium)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.3f)
                lineTo(w * 0.5f, h * 0.72f)
                lineTo(w * 0.85f, h * 0.3f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

