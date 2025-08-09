package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_remaining_time
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.text.DecoratedText
import org.techascent.composa.text.NormalText
import org.techascent.composa.text.SpannableText
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.formatDuration

@Composable
fun CountdownTimerWithProgress(
    currentPrayer: StringResource,
    targetTime: Instant,
    totalDuration: Duration,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ComposaTheme.color.strokeNeutralSubtle
    val foregroundColor = ComposaTheme.color.iconAction
    var remainingTime by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(targetTime) {
        while (true) {
            val now = Clock.System.now()
            val duration = targetTime - now
            remainingTime = if (duration.isPositive()) duration else Duration.ZERO
            delay(1000)
        }
    }

    val formattedRemainingTime = remainingTime.formatDuration()
    val progress = remember(remainingTime) {
        val fraction =
            1f - (remainingTime.inWholeMilliseconds.toFloat() / totalDuration.inWholeMilliseconds.toFloat())
        fraction.coerceIn(0f, 1f)
    }
    ComposaCardFrame(
        modifier = modifier,
        borderColor = ComposaTheme.color.strokeNeutralSubtle,
        content = {
            SpannableText(
                modifier = Modifier.padding(
                    top = ComposaSpacing.Medium,
                    start = ComposaSpacing.Medium,
                    end = ComposaSpacing.Medium
                ),
                segments = listOf(
                    NormalText(
                        stringResource(resource = Res.string.text_remaining_time).plus(" "),
                        order = 0
                    ),
                    DecoratedText(
                        text = stringResource(resource = currentPrayer),
                        order = 0,
                        spanStyle = SpanStyle(
                            color = ComposaTheme.color.iconAction,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )

                    )
                ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(175.dp).padding(ComposaSpacing.Medium),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = ComposaSpacing.Medium.toPx()
                    val radiusOffset = strokeWidth / 2
                    val size = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(radiusOffset, radiusOffset)

                    // Background circle
                    drawArc(
                        color = backgroundColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = size,
                        style = Stroke(strokeWidth)
                    )

                    // Foreground progress
                    drawArc(
                        color = foregroundColor, // Compose green
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = size,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Timer text
                Text(
                    text = formattedRemainingTime,
                    style = ComposaTheme.typography.bodyEmphasized
                )
            }
        }
    )


}