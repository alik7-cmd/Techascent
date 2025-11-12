package org.techascent.muslim.tasbeeh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.title_tasbeeh_instruction
import kotlin.math.pow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.tasbeeh.state.TasbeehUiState

fun LazyListScope.parabolicTasbeeh(
    uiState: TasbeehUiState
) {
    item {
        var count by remember { mutableStateOf(0) }
        val animProgress = remember { Animatable(0f) }
        var isAnimating by remember { mutableStateOf(false) }

        val totalWidth = 500f
        val spacing = 90f
        val baseHeight = 250f
        val arcHeight = 120f
        val beadRadius = 40f

        // Fixed bead count
        val leftCount = 4
        val rightCount = 4

        // Animatables for spring-like bounce of left/right beads
        val leftShift = remember { Animatable(0f) }
        val rightShift = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        // Launch animation when triggered
        LaunchedEffect(isAnimating) {
            if (isAnimating) {
                animProgress.snapTo(0f)
                animProgress.animateTo(1f, tween(600))
                count++

                // Spring-like bounce for left/right beads
                scope.launch {
                    leftShift.animateTo(
                        targetValue = spacing / leftCount,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                }
                scope.launch {
                    rightShift.animateTo(
                        targetValue = -spacing / rightCount,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                }

                // Reset shifts instantly after bounce
                leftShift.snapTo(0f)
                rightShift.snapTo(0f)

                animProgress.snapTo(0f)
                isAnimating = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(400.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (isAnimating) return@detectHorizontalDragGestures
                        change.consume()
                        if (dragAmount > 0) isAnimating = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2

                // Left beads with spring shift
                val leftBeads = (0 until leftCount).map { i ->
                    val shift =
                        leftShift.value + if (isAnimating) animProgress.value * spacing / leftCount else 0f
                    val x = centerX - spacing * (leftCount - i) + shift
                    val y = baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                    Offset(x, y)
                }

                // Right beads with spring shift
                val rightBeads = (0 until rightCount).map { i ->
                    val shift =
                        rightShift.value - if (isAnimating) animProgress.value * spacing / rightCount else 0f
                    val x = centerX + spacing * (i + 1) + shift
                    val y = baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                    Offset(x, y)
                }

                // Moving bead
                val movingBead = if (isAnimating) {
                    val t = animProgress.value
                    val startX = centerX - spacing * leftCount
                    val endX = centerX + spacing
                    val x = lerp(startX, endX, t)
                    val y = baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                    Offset(x, y)
                } else null

                // All beads for string
                val allBeads =
                    leftBeads + (movingBead?.let { listOf(it) } ?: emptyList()) + rightBeads

                // Draw string (1px)
                if (allBeads.size > 1) {
                    val path = Path().apply {
                        moveTo(allBeads.first().x, allBeads.first().y)
                        for (p in allBeads.drop(1)) lineTo(p.x, p.y)
                    }
                    drawPath(path, Color.Gray, style = Stroke(width = 1f))
                }

                // Draw beads
                leftBeads.forEach { drawCircle(Color(0xFF4CAF50), beadRadius, it) }
                rightBeads.forEach { drawCircle(Color(0xFFAAAAAA), beadRadius, it) }
                movingBead?.let { drawCircle(Color.Yellow, beadRadius + 6f, it) }
            }

            // Counter text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // <-- THIS IS THE KEY MODIFIER
                    .padding(bottom = ComposaSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = count.toString(),
                    style = ComposaTheme.typography.titleLargeDemi
                )

                Spacer(modifier = Modifier.height(ComposaSpacing.Large))

                MessageBox(
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium)
                        .testTag(PrayerTags.PRAYER_TIME_INFO_CONTENT),
                    messageType = MessageType.Info,
                    message = stringResource(Res.string.title_tasbeeh_instruction)
                )

                Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ComposaSpacing.Medium),
                    text = stringResource(resource = uiState.infoMessage),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutral
                )
            }
        }
    }

}

private fun lerp(start: Float, end: Float, t: Float): Float = start + (end - start) * t
