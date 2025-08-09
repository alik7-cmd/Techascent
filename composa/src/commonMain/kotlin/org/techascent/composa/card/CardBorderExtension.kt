package org.techascent.composa.card

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public fun Modifier.topBorder(
    cornerRadius: Dp,
    strokeWidth: Dp,
    color: Color,
): Modifier {
    return this
        .drawWithContent {
            val path = Path()
            path.moveTo(x = 0.dp.toPx(), y = this.size.height)
            path.arcTo(
                rect = Rect(
                    offset = Offset(
                        x = 0.dp.toPx(),
                        y = strokeWidth.toPx()
                    ),
                    size = Size(
                        width = cornerRadius.toPx(),
                        height = cornerRadius.toPx()
                    )
                ),
                startAngleDegrees = -180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            path.arcTo(
                rect = Rect(
                    offset = Offset(
                        x = this.size.width - cornerRadius.toPx(),
                        y = strokeWidth.toPx()
                    ),
                    size = Size(
                        width = cornerRadius.toPx(),
                        height = cornerRadius.toPx()
                    )
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            path.lineTo(this.size.width, this.size.height)
            val contentDrawScope = this
            clipPath(path) {
                contentDrawScope.drawContent()
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(strokeWidth.toPx())
            )
        }
}

public fun Modifier.bottomBorder(
    cornerRadius: Dp,
    strokeWidth: Dp,
    color: Color,
): Modifier {
    return this
        .drawWithContent {
            val path = Path()
            path.moveTo(x = 0f, y = 0f)
            path.arcTo(
                rect = Rect(
                    offset = Offset(
                        x = 0f,
                        y = this.size.height - cornerRadius.toPx() - strokeWidth.toPx()
                    ),
                    size = Size(cornerRadius.toPx(), cornerRadius.toPx())
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            path.arcTo(
                rect = Rect(
                    offset = Offset(
                        x = this.size.width - cornerRadius.toPx(),
                        y = this.size.height - cornerRadius.toPx() - strokeWidth.toPx()
                    ),
                    size = Size(
                        width = cornerRadius.toPx(),
                        height = cornerRadius.toPx()
                    )
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            path.lineTo(x = this.size.width, y = 0f)
            val contentDrawScope = this
            clipPath(path) {
                contentDrawScope.drawContent()
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(strokeWidth.toPx())
            )
        }
}


public fun Modifier.sideBorder(
    strokeWidth: Dp,
    color: Color,
): Modifier {
    return this
        .drawWithContent {
            val contentDrawScope = this
            clipRect {
                contentDrawScope.drawContent()
            }
            drawLine(
                color = color,
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = this.size.height),
                strokeWidth = strokeWidth.toPx(),
            )
            drawLine(
                color = color,
                start = Offset(x = this.size.width, y = 0f),
                end = Offset(x = this.size.width, y = this.size.height),
                strokeWidth = strokeWidth.toPx(),
            )
        }
}
