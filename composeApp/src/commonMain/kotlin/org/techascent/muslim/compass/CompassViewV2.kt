package org.techascent.muslim.compass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_qibla_aligned
import apphub.composeapp.generated.resources.text_qibla_subtitle
import apphub.composeapp.generated.resources.text_qibla_tip
import apphub.composeapp.generated.resources.text_qibla_tip_title
import apphub.composeapp.generated.resources.title_quibla
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── Qibla-green accent ──────────────────────────────────────────────────────────
private val QiblaGreen = Color(0xFF2E7D32)

// ── Threshold in degrees to consider "aligned" ──────────────────────────────────
private const val ALIGNED_THRESHOLD = 8f

@Composable
fun CompassViewV2(
    onNavigateBack: () -> Unit,
) {
    ComposaTheme {
        CompassScreenV2(onNavigateBack = onNavigateBack)
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun CompassScreenV2(
    viewModel: CompassViewModel = koinViewModel<CompassViewModel>(),
    onNavigateBack: () -> Unit,
) {
    CompassContentV2(
        quiblaDirectionFlow = viewModel.qiblaDirection,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompassContentV2(
    quiblaDirectionFlow: Flow<Float>,
    onNavigateBack: () -> Unit,
) {
    val direction by quiblaDirectionFlow.collectAsState(initial = 0f)
    val animatedDirection by animateFloatAsState(
        targetValue = direction,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 80f),
        label = "compassRotation",
    )

    val isAligned = abs(direction) < ALIGNED_THRESHOLD || abs(direction - 360f) < ALIGNED_THRESHOLD

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_quibla),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ComposaSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.06f))

            // ── Subtitle ────────────────────────────────────────────────────
            Text(
                text = stringResource(Res.string.text_qibla_subtitle),
                style = ComposaTheme.typography.subhead,
                color = ComposaTheme.color.textNeutralSubtle,
            )

            Spacer(modifier = Modifier.weight(0.06f))

            // ── Compass dial ────────────────────────────────────────────────
            QiblaCompassDial(
                rotationDegrees = animatedDirection,
                isAligned = isAligned,
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Large))

            // ── Alignment status ────────────────────────────────────────────
            AlignmentIndicator(isAligned = isAligned, degrees = direction)

            Spacer(modifier = Modifier.weight(0.08f))

            // ── Tip card ────────────────────────────────────────────────────
            TipCard()

            Spacer(modifier = Modifier.weight(0.06f))
        }
    }
}

// ─── Compass dial ───────────────────────────────────────────────────────────────

@Composable
private fun QiblaCompassDial(
    rotationDegrees: Float,
    isAligned: Boolean,
) {
    val compassSize = responsiveDp(fraction = 0.72f)

    val accentColor by animateColorAsState(
        targetValue = if (isAligned) QiblaGreen else ComposaTheme.color.textNeutralSubtle,
        animationSpec = tween(400),
        label = "accentColor",
    )

    val outerRingColor = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.3f)
    val tickColor = ComposaTheme.color.textNeutralSubtle
    val cardinalColor = ComposaTheme.color.textNeutral
    val bgColor = ComposaTheme.color.backgroundAppBackground

    // Pulsing glow when aligned
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(compassSize),
    ) {
        // ── Fixed Qibla pointer at the top ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(24.dp)
                .drawBehind {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path, color = accentColor)
                }
        )

        // ── Rotating compass face ───────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(compassSize - 32.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = accentColor.copy(alpha = 0.12f),
                    spotColor = accentColor.copy(alpha = 0.12f),
                )
                .clip(CircleShape)
                .background(bgColor)
                .drawBehind {
                    drawCompassFace(
                        rotationDegrees = rotationDegrees,
                        outerRingColor = outerRingColor,
                        tickColor = tickColor,
                        cardinalColor = cardinalColor,
                        accentColor = accentColor,
                        isAligned = isAligned,
                        pulseAlpha = pulseAlpha,
                    )
                },
        ) {
            // ── Kaaba icon / center dot ─────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
            ) {
                // Kaaba symbol ◼ (Unicode)
                Text(
                    text = "🕋",
                    fontSize = 26.sp,
                )
            }
        }
    }
}

// ─── Draw compass face (ticks, labels, Qibla arrow) ─────────────────────────────

private fun DrawScope.drawCompassFace(
    rotationDegrees: Float,
    outerRingColor: Color,
    tickColor: Color,
    cardinalColor: Color,
    accentColor: Color,
    isAligned: Boolean,
    pulseAlpha: Float,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = size.minDimension / 2f

    // ── Outer ring ──────────────────────────────────────────────────────
    drawCircle(
        color = outerRingColor,
        radius = radius - 4f,
        center = Offset(cx, cy),
        style = Stroke(width = 2f),
    )

    // ── Inner decorative ring ───────────────────────────────────────────
    drawCircle(
        color = outerRingColor.copy(alpha = 0.2f),
        radius = radius - 28f,
        center = Offset(cx, cy),
        style = Stroke(width = 1f),
    )

    // ── Glow ring when aligned ──────────────────────────────────────────
    if (isAligned) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = pulseAlpha),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = radius,
            ),
            radius = radius,
            center = Offset(cx, cy),
        )
    }

    // ── Rotate canvas for ticks & labels ────────────────────────────────
    rotate(degrees = -rotationDegrees, pivot = Offset(cx, cy)) {

        val tickRadius = radius - 12f
        val longTickLen = 18f
        val shortTickLen = 10f

        for (deg in 0 until 360 step 5) {
            val isCardinal = deg % 90 == 0
            val isMajor = deg % 30 == 0
            val len = when {
                isCardinal -> longTickLen + 4f
                isMajor -> longTickLen
                else -> shortTickLen
            }
            val angleRad = (deg.toDouble() * PI / 180.0).toFloat()

            val outerX = cx + tickRadius * sin(angleRad)
            val outerY = cy - tickRadius * cos(angleRad)
            val innerX = cx + (tickRadius - len) * sin(angleRad)
            val innerY = cy - (tickRadius - len) * cos(angleRad)

            val color = when {
                deg == 0 -> Color(0xFFEF5350)  // North → red
                isCardinal -> cardinalColor
                isMajor -> tickColor.copy(alpha = 0.6f)
                else -> tickColor.copy(alpha = 0.25f)
            }

            drawLine(
                color = color,
                start = Offset(outerX, outerY),
                end = Offset(innerX, innerY),
                strokeWidth = if (isCardinal) 3f else if (isMajor) 2f else 1f,
                cap = StrokeCap.Round,
            )
        }

        // ── Cardinal labels ─────────────────────────────────────────────
        val labelRadius = radius - 44f
        val cardinals = listOf(
            0f to "N",
            90f to "E",
            180f to "S",
            270f to "W",
        )

        cardinals.forEach { (deg, _) ->
            val angleRad = (deg.toDouble() * PI / 180.0).toFloat()
            val lx = cx + labelRadius * sin(angleRad)
            val ly = cy - labelRadius * cos(angleRad)
            val isNorth = deg == 0f

            // Small dot for cardinal positions
            drawCircle(
                color = if (isNorth) Color(0xFFEF5350) else cardinalColor.copy(alpha = 0.7f),
                radius = if (isNorth) 6f else 4f,
                center = Offset(lx, ly),
            )
        }

        // ── Qibla arrow (points upward in un-rotated space = toward fixed pointer) ──
        val arrowLength = radius - 60f
        val arrowWidth = 16f

        // The Qibla is at 0° in the dial space since the whole dial rotates
        // so the direction is always at the top
        val arrowPath = Path().apply {
            // Arrow head
            moveTo(cx, cy - arrowLength)
            lineTo(cx - arrowWidth, cy - arrowLength + 36f)
            lineTo(cx - 4f, cy - arrowLength + 28f)
            // Shaft
            lineTo(cx - 4f, cy + 20f)
            lineTo(cx + 4f, cy + 20f)
            lineTo(cx + 4f, cy - arrowLength + 28f)
            // Arrow head right
            lineTo(cx + arrowWidth, cy - arrowLength + 36f)
            close()
        }

        // Shadow under arrow
        drawPath(
            path = arrowPath,
            color = accentColor.copy(alpha = 0.1f),
        )
    }

    // ── Small center dot ────────────────────────────────────────────────
    drawCircle(
        color = accentColor,
        radius = 5f,
        center = Offset(cx, cy),
    )
}

// ─── Alignment indicator ────────────────────────────────────────────────────────

@Composable
private fun AlignmentIndicator(isAligned: Boolean, degrees: Float) {
    val bgColor by animateColorAsState(
        targetValue = if (isAligned) QiblaGreen.copy(alpha = 0.12f)
        else ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.18f),
        animationSpec = tween(400),
        label = "indicatorBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isAligned) QiblaGreen else ComposaTheme.color.textNeutral,
        animationSpec = tween(400),
        label = "indicatorText",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isAligned) {
            // Check-mark
            Text(
                text = "✓",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = QiblaGreen,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.text_qibla_aligned),
                style = ComposaTheme.typography.subheadEmphasized,
                color = QiblaGreen,
            )
        } else {
            Text(
                text = "🕋",
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${degrees.toInt()}° to Qibla",
                style = ComposaTheme.typography.subheadEmphasized,
                color = textColor,
            )
        }
    }
}

// ─── Tip card ───────────────────────────────────────────────────────────────────

@Composable
private fun TipCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.12f))
            .padding(ComposaSpacing.Medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.text_qibla_tip_title),
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.text_qibla_tip),
            style = ComposaTheme.typography.footnote,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Start,
        )
    }
}

