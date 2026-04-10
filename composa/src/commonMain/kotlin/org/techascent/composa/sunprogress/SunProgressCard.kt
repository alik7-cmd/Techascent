package org.techascent.composa.sunprogress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A generic sun/moon progress card that renders a sky-gradient background
 * with an arc showing celestial-body progress. Supports day, dawn, dusk,
 * and night phases with appropriate sun/moon rendering.
 *
 * Use [headerContent] to place any composable (e.g. location + prayer info)
 * above the arc canvas.
 *
 * @param config Visual configuration — colours, progress, phase flags.
 * @param modifier Modifier applied to the outer column.
 * @param headerContent Optional composable slot rendered above the canvas.
 */
@Composable
fun SunProgressCard(
    config: SunProgressConfig,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
) {
    val isNight = config.isNight
    val isDusk = config.isDusk
    val isDawn = config.isDawn
    val dayProgress = config.dayProgress

    // Pseudo-random stars (stable per composition)
    val stars = remember {
        List(28) {
            Triple(
                (it * 37 + 13) % 100 / 100f,   // x ratio
                (it * 53 + 7) % 100 / 100f,     // y ratio
                1f + (it % 3) * 0.6f             // radius
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(config.skyGradient)),
    ) {
        // ── Optional header slot ──────────────────────────────────────
        headerContent?.invoke()

        // ── Canvas — arc + celestial body ─────────────────────────────
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.85f
            val arcPadding = w * 0.08f
            val arcWidth = w - 2 * arcPadding
            val arcHeight = h * 0.72f

            // ─ Stars (only during night / dusk / dawn) ────────────────
            if (isNight || isDusk || isDawn) {
                val starAlpha = when {
                    isNight -> 0.85f
                    isDusk -> 0.5f
                    isDawn -> 0.35f
                    else -> 0f
                }
                stars.forEach { (xr, yr, r) ->
                    val sy = yr * horizonY * 0.9f
                    val sx = xr * w
                    drawCircle(
                        config.starColor.copy(alpha = starAlpha * (0.5f + r / 3f)),
                        radius = r,
                        center = Offset(sx, sy),
                    )
                }
            }

            // ─ Horizon line ───────────────────────────────────────────
            val horizonAlpha = if (isNight) 0.3f else 1f
            drawLine(
                config.horizonColor.copy(alpha = horizonAlpha),
                Offset(0f, horizonY),
                Offset(w, horizonY),
                strokeWidth = 1.5f,
            )

            // ─ Arc track ──────────────────────────────────────────────
            val arcPath = Path()
            val steps = 120
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val angle = PI * (1 - t)
                val x = arcPadding + arcWidth * t
                val y = horizonY - arcHeight * sin(angle).toFloat()
                if (i == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
            }
            drawPath(
                arcPath,
                config.arcTrackColor.copy(alpha = if (isNight) 0.15f else 0.3f),
                style = Stroke(width = 2f, cap = StrokeCap.Round),
            )

            // ─ Traveled arc ───────────────────────────────────────────
            val traveledPath = Path()
            val travelSteps = (dayProgress * steps).toInt()
            for (i in 0..travelSteps) {
                val t = i.toFloat() / steps
                val angle = PI * (1 - t)
                val x = arcPadding + arcWidth * t
                val y = horizonY - arcHeight * sin(angle).toFloat()
                if (i == 0) traveledPath.moveTo(x, y) else traveledPath.lineTo(x, y)
            }
            drawPath(
                traveledPath,
                config.arcAccentColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round),
            )

            // ─ Celestial body position ────────────────────────────────
            val bodyAngle = PI * (1 - dayProgress)
            val bodyX = arcPadding + arcWidth * dayProgress
            val bodyY = horizonY - arcHeight * sin(bodyAngle).toFloat()

            if (isNight || isDusk) {
                // ═══ MOON ═════════════════════════════════════════════
                drawCircle(config.glowColor.copy(alpha = 0.10f), radius = 44f, center = Offset(bodyX, bodyY))
                drawCircle(config.glowColor.copy(alpha = 0.18f), radius = 32f, center = Offset(bodyX, bodyY))
                drawCircle(config.glowColor.copy(alpha = 0.28f), radius = 22f, center = Offset(bodyX, bodyY))

                // Moon body
                drawCircle(config.bodyColor, radius = 16f, center = Offset(bodyX, bodyY))

                // Crescent shadow
                drawCircle(
                    color = if (isNight) config.nightSkyStartColor.copy(alpha = 0.7f)
                    else config.twilightSkyStartColor.copy(alpha = 0.6f),
                    radius = 13f,
                    center = Offset(bodyX + 7f, bodyY - 4f),
                )

                // Craters
                drawCircle(config.craterColor, radius = 2.5f, center = Offset(bodyX - 5f, bodyY + 2f))
                drawCircle(config.craterColor, radius = 1.8f, center = Offset(bodyX - 2f, bodyY - 6f))
                drawCircle(config.craterColor, radius = 1.5f, center = Offset(bodyX + 1f, bodyY + 5f))

                // Highlight
                drawCircle(Color.White.copy(alpha = 0.18f), radius = 5f, center = Offset(bodyX - 6f, bodyY - 5f))
            } else {
                // ═══ SUN ══════════════════════════════════════════════
                val glowAlpha = if (isDawn) 0.5f else 1f
                drawCircle(config.glowColor.copy(alpha = 0.08f * glowAlpha), radius = 44f, center = Offset(bodyX, bodyY))
                drawCircle(config.glowColor.copy(alpha = 0.15f * glowAlpha), radius = 32f, center = Offset(bodyX, bodyY))
                drawCircle(config.glowColor.copy(alpha = 0.30f * glowAlpha), radius = 22f, center = Offset(bodyX, bodyY))

                // Rays
                val rayAlpha = if (isDawn) 0.3f else 0.6f
                val rayCount = 12
                val innerR = 16f
                val outerR = 28f
                for (r in 0 until rayCount) {
                    val rayAngle = 2.0 * PI * r / rayCount
                    val x1 = bodyX + innerR * cos(rayAngle).toFloat()
                    val y1 = bodyY + innerR * sin(rayAngle).toFloat()
                    val x2 = bodyX + outerR * cos(rayAngle).toFloat()
                    val y2 = bodyY + outerR * sin(rayAngle).toFloat()
                    drawLine(
                        config.bodyColor.copy(alpha = rayAlpha),
                        Offset(x1, y1), Offset(x2, y2),
                        strokeWidth = 2f, cap = StrokeCap.Round,
                    )
                }

                // Sun body
                drawCircle(config.bodyColor, radius = 14f, center = Offset(bodyX, bodyY))
                // Highlight
                drawCircle(Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(bodyX - 3f, bodyY - 3f))
            }

            // Endpoint dots
            drawCircle(config.arcTrackColor, radius = 4f, center = Offset(arcPadding, horizonY))
            drawCircle(config.arcTrackColor, radius = 4f, center = Offset(arcPadding + arcWidth, horizonY))
        }

        Spacer(Modifier.height(ComposaSpacing.Small))
    }
}

/** Linearly interpolate between two colours by [fraction] (0→a, 1→b). */
fun lerpColor(a: Color, b: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f,
    )
}

