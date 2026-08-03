package org.techascent.muslim.tasbeeh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_tasbeeh_set_complete
import apphub.composeapp.generated.resources.title_tasbeeh_instruction
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.muslim.common.localizeDigits
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.performHapticFeedback
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.tasbeeh.state.TasbeehUiState
import kotlin.math.pow

fun LazyListScope.parabolicTasbeeh(
    uiState: TasbeehUiState,
    onCounterIncrement: () -> Unit,
    onSetComplete: () -> Unit, // 1. Add a new lambda for completing a set
) {
    item {
        val animProgress = remember { Animatable(0f) }
        var isAnimating by remember { mutableStateOf(false) }

        val progressTarget = if (uiState.goal > 0) (uiState.count % uiState.goal).toFloat() / uiState.goal else 0f
        val animatedProgress by animateFloatAsState(
            targetValue = progressTarget,
            animationSpec = spring(),
            label = "progressAnimation",
        )

        val totalWidth = 500f
        val spacing = 90f
        val baseHeight = 250f
        val arcHeight = 120f
        val beadRadius = 40f

        val leftCount = 4
        val rightCount = 4

        val leftShift = remember { Animatable(0f) }
        val rightShift = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(isAnimating) {
            if (isAnimating) {
                animProgress.snapTo(0f)
                animProgress.animateTo(1f, tween(600))

                // 2. Check if the goal has been reached
                if(uiState.haptic){
                    performHapticFeedback()
                }
                if (uiState.count + 1 == uiState.goal) {


                    onSetComplete() // Call the new function to increment sets and reset
                } else {
                    onCounterIncrement() // Otherwise, just increment the counter
                }


                scope.launch {
                    leftShift.animateTo(
                        targetValue = spacing / leftCount,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    )
                }
                scope.launch {
                    rightShift.animateTo(
                        targetValue = -spacing / rightCount,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    )
                }

                leftShift.snapTo(0f)
                rightShift.snapTo(0f)

                animProgress.snapTo(0f)
                isAnimating = false
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillParentMaxHeight()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (isAnimating) return@detectHorizontalDragGestures
                        change.consume()
                        if (dragAmount > 0) isAnimating = true
                    }
                },
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(150.dp),
                        color = ComposaTheme.color.textAction,
                        strokeWidth = 12.dp,
                    )
                    Text(
                        // 3. Display the count within the goal (e.g., 32 instead of 65)
                        text = "${uiState.count % uiState.goal}/${uiState.goal}".localizeDigits(),
                        style = ComposaTheme.typography.titleLargeDemi,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Give the canvas a fixed height
                    contentAlignment = Alignment.Center,
                ) {
                    // Capture theme colors here — Canvas DrawScope is not a composable context
                    val beadStringColor = ComposaTheme.color.prayer.tasbeehInactive
                    val beadInactiveColor = ComposaTheme.color.prayer.tasbeehInactive
                    val beadActiveColor = ComposaTheme.color.prayer.tasbeehActive
                    val beadHighlightColor = ComposaTheme.color.prayer.tasbeehHighlight

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        // Your canvas drawing logic remains the same...
                        // Left beads
                        val leftBeads = (0 until leftCount).map { i ->
                            val shift =
                                leftShift.value + if (isAnimating) animProgress.value * spacing / leftCount else 0f
                            val x = centerX - spacing * (leftCount - i) + shift
                            val y =
                                baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                            Offset(x, y)
                        }

                        // Right beads
                        val rightBeads = (0 until rightCount).map { i ->
                            val shift =
                                rightShift.value - if (isAnimating) animProgress.value * spacing / rightCount else 0f
                            val x = centerX + spacing * (i + 1) + shift
                            val y =
                                baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                            Offset(x, y)
                        }

                        // Moving bead
                        val movingBead = if (isAnimating) {
                            val t = animProgress.value
                            val startX = centerX - spacing * leftCount
                            val endX = centerX + spacing
                            val x = lerp(startX, endX, t)
                            val y =
                                baseHeight - arcHeight * ((x - centerX).pow(2) / totalWidth.pow(2))
                            Offset(x, y)
                        } else {
                            null
                        }

                        val allBeads =
                            leftBeads + (movingBead?.let { listOf(it) } ?: emptyList()) + rightBeads

                        if (allBeads.size > 1) {
                            val path = Path().apply {
                                moveTo(allBeads.first().x, allBeads.first().y)
                                for (p in allBeads.drop(1)) lineTo(p.x, p.y)
                            }
                            drawPath(path,
                                beadStringColor, style = Stroke(width = 1f)
                            )
                        }

                        leftBeads.forEach { drawCircle(beadInactiveColor, beadRadius, it) }
                        rightBeads.forEach { drawCircle(beadActiveColor, beadRadius, it) }
                        movingBead?.let { drawCircle(beadHighlightColor, beadRadius + 6f, it) }
                    }
                }

                // --- 3. Bottom Information Panel ---
                // This is placed directly below the canvas within the same Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Medium)
                ) {
                    // 4. Display the Set count
                    if (uiState.sets > 0) {
                        Text(
                            text = stringResource(Res.string.text_tasbeeh_set_complete,
                                uiState.sets,
                                (uiState.sets * uiState.goal).plus(uiState.count)
                            ),
                            style = ComposaTheme.typography.body,
                            color = ComposaTheme.color.textAction,
                            modifier = Modifier.padding(bottom = ComposaSpacing.Small)
                        )
                    }

                    MessageBox(
                        modifier = Modifier
                            .padding(horizontal = ComposaSpacing.Medium)
                            .testTag(PrayerTags.PRAYER_TIME_INFO_CONTENT),
                        messageType = MessageType.Info,
                        message = stringResource(Res.string.title_tasbeeh_instruction),
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ComposaSpacing.Medium),
                        text = stringResource(resource = uiState.infoMessage),
                        style = ComposaTheme.typography.footnote,
                        color = ComposaTheme.color.textNeutral,
                    )
                }
            }
        }
    }
}

private fun lerp(start: Float, end: Float, t: Float): Float = start + (end - start) * t
