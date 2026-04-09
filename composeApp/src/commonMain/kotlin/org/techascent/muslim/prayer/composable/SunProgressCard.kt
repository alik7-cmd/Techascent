package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.localizeDigits
import org.techascent.muslim.common.localizeTime
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═════════════════════════════════════════════════════════════════════════════════
//  1. SUN / MOON PROGRESS HERO — dynamic day/night with sky dimming
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * Determines the "phase of day" to pick the right visuals:
 *  - NIGHT   : after sunset or before fajr
 *  - DAWN    : fajr → sunrise  (twilight brightening)
 *  - DAY     : sunrise → sunset
 *  - DUSK    : sunset → isha   (twilight dimming)
 */
private enum class DayPhase { NIGHT, DAWN, DAY, DUSK }

@Composable
internal fun SunProgressCard(uiModel: PrayerTimeUiModel) {
    val prayer = uiModel.currentPrayer
    val prayerColors = ComposaTheme.color.prayer
    val now = Clock.System.now()

    val sunriseInstant = uiModel.sunriseInstant
    val sunsetInstant = uiModel.sunsetInstant
    val fajrStart =
        uiModel.intervals.firstOrNull { it.name == PrayerNameEnum.FAJR }?.startTimeInstant
    val ishaStart =
        uiModel.intervals.firstOrNull { it.name == PrayerNameEnum.ISHA }?.startTimeInstant
    val ishaEnd = uiModel.intervals.firstOrNull { it.name == PrayerNameEnum.ISHA }?.endTimeInstant

    // ── Determine phase ──────────────────────────────────────────────
    // DAWN  = Fajr start → Sunrise         (pre-dawn twilight)
    // DAY   = Sunrise → Sunset             (sun visible)
    // DUSK  = Sunset → Isha start          (twilight after sunset, Maghrib time)
    // NIGHT = Isha start → next Fajr       (full night, Isha time and beyond)
    val phase = remember(now, sunriseInstant, sunsetInstant, fajrStart, ishaStart) {
        when {
            sunriseInstant == null || sunsetInstant == null -> DayPhase.DAY
            fajrStart != null && now >= fajrStart && now < sunriseInstant -> DayPhase.DAWN
            now >= sunriseInstant && now < sunsetInstant -> DayPhase.DAY
            ishaStart != null && now >= sunsetInstant && now < ishaStart -> DayPhase.DUSK
            else -> DayPhase.NIGHT   // Isha time and beyond = night
        }
    }

    val isNight = phase == DayPhase.NIGHT
    val isDusk = phase == DayPhase.DUSK
    val isDawn = phase == DayPhase.DAWN

    // ── Dynamic sky gradient ─────────────────────────────────────────
    val skyGradient = when (phase) {
        DayPhase.DAY -> {
            // Dim towards sunset: lerp from full-day to twilight
            if (sunriseInstant != null && sunsetInstant != null) {
                val totalDay = (sunsetInstant - sunriseInstant).inWholeMilliseconds.toFloat()
                val elapsed = (now - sunriseInstant).inWholeMilliseconds.toFloat()
                val t = (elapsed / totalDay).coerceIn(0f, 1f)
                // After 80 % of the day, start dimming
                val dimFactor = if (t > 0.8f) ((t - 0.8f) / 0.2f).coerceIn(0f, 1f) else 0f
                val s = lerpColor(prayerColors.skyStart, prayerColors.twilightSkyStart, dimFactor)
                val e = lerpColor(prayerColors.skyEnd, prayerColors.twilightSkyEnd, dimFactor)
                listOf(s, e)
            } else listOf(prayerColors.skyStart, prayerColors.skyEnd)
        }

        DayPhase.DAWN -> listOf(prayerColors.dawnSkyStart, prayerColors.dawnSkyEnd)
        DayPhase.DUSK -> listOf(prayerColors.twilightSkyStart, prayerColors.twilightSkyEnd)
        DayPhase.NIGHT -> listOf(prayerColors.nightSkyStart, prayerColors.nightSkyEnd)
    }

    // ── Progress along the arc (phase-aware) ─────────────────────────
    // Each phase uses its own time window so the celestial body moves
    // correctly across the arc for that specific period.
    val dayProgress = when (phase) {
        DayPhase.DAWN -> {
            // Fajr start → Sunrise
            if (fajrStart != null && sunriseInstant != null && sunriseInstant > fajrStart) {
                val total = (sunriseInstant - fajrStart).inWholeMilliseconds.toFloat()
                val elapsed = (now - fajrStart).inWholeMilliseconds.toFloat()
                (elapsed / total).coerceIn(0f, 1f)
            } else 0.5f
        }

        DayPhase.DAY -> {
            // Sunrise → Sunset
            if (sunriseInstant != null && sunsetInstant != null && sunsetInstant > sunriseInstant) {
                val total = (sunsetInstant - sunriseInstant).inWholeMilliseconds.toFloat()
                val elapsed = (now - sunriseInstant).inWholeMilliseconds.toFloat()
                (elapsed / total).coerceIn(0f, 1f)
            } else 0.5f
        }

        DayPhase.DUSK -> {
            // Sunset → Isha start
            if (sunsetInstant != null && ishaStart != null && ishaStart > sunsetInstant) {
                val total = (ishaStart - sunsetInstant).inWholeMilliseconds.toFloat()
                val elapsed = (now - sunsetInstant).inWholeMilliseconds.toFloat()
                (elapsed / total).coerceIn(0f, 1f)
            } else 0.5f
        }

        DayPhase.NIGHT -> {
            // Isha start → Isha end (moon drifts slowly)
            if (ishaStart != null && ishaEnd != null && ishaEnd > ishaStart) {
                val total = (ishaEnd - ishaStart).inWholeMilliseconds.toFloat()
                val elapsed = (now - ishaStart).inWholeMilliseconds.toFloat()
                (elapsed / total).coerceIn(0f, 1f)
            } else 0.5f
        }
    }

    // ── Resolve colours per phase ────────────────────────────────────
    val bodyColor = if (isNight || isDusk) prayerColors.moonBody else prayerColors.sunBody
    val glowColor = if (isNight || isDusk) prayerColors.moonGlow else prayerColors.sunGlow
    val horizonColor = prayerColors.horizon
    val accentColor = prayerColors.timerAccent
    val trackColor = prayerColors.timerTrack
    val starColor = prayerColors.starColor
    val craterColor = prayerColors.moonCrater

    // Text colour adapts: light text on dark bg
    val textOnSky =
        if (isNight || isDusk || isDawn) Color.White.copy(alpha = 0.9f) else ComposaTheme.color.textNeutral
    val subtleOnSky =
        if (isNight || isDusk || isDawn) Color.White.copy(alpha = 0.6f) else ComposaTheme.color.textNeutralSubtle

    // ── Pseudo-random stars (stable per session) ─────────────────────
    val stars = remember {
        List(28) {
            Triple(
                (it * 37 + 13) % 100 / 100f,        // x ratio
                (it * 53 + 7) % 100 / 100f,         // y ratio
                1f + (it % 3) * 0.6f                 // radius
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(skyGradient)),
    ) {
        // ── Header: location + prayer info ───────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .padding(top = ComposaSpacing.Small),
        ) {
            Text(
                buildString {
                    append(
                        uiModel.addressInfo.district?.plus(", ${uiModel.addressInfo.country}")
                            ?: uiModel.addressInfo.address
                    )
                    append("  •  ")
                    append(uiModel.hijriDate)
                }.localizeDigits(),
                style = ComposaTheme.typography.caption,
                color = subtleOnSky,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            prayer?.let {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(it.name.toDisplayString()),
                        style = ComposaTheme.typography.titleEmphasized,
                        color = textOnSky,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${it.displayableStartTime} – ${it.displayableEndTime}".localizeTime(),
                        style = ComposaTheme.typography.footnote,
                        color = subtleOnSky,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        // ── Canvas — arc + celestial body ────────────────────────────
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.85f
            val arcPadding = w * 0.08f
            val arcWidth = w - 2 * arcPadding
            val arcHeight = h * 0.72f

            // ─ Stars (only during night / dusk / dawn) ───────────────
            if (isNight || isDusk || isDawn) {
                val starAlpha = when (phase) {
                    DayPhase.NIGHT -> 0.85f
                    DayPhase.DUSK -> 0.5f
                    DayPhase.DAWN -> 0.35f
                    else -> 0f
                }
                stars.forEach { (xr, yr, r) ->
                    // Restrict stars above the horizon
                    val sy = yr * horizonY * 0.9f
                    val sx = xr * w
                    drawCircle(
                        starColor.copy(alpha = starAlpha * (0.5f + r / 3f)),
                        radius = r,
                        center = Offset(sx, sy),
                    )
                }
            }

            // ─ Horizon line ──────────────────────────────────────────
            val horizonAlpha = if (isNight) 0.3f else 1f
            drawLine(
                horizonColor.copy(alpha = horizonAlpha),
                Offset(0f, horizonY),
                Offset(w, horizonY),
                strokeWidth = 1.5f
            )

            // ─ Arc track ─────────────────────────────────────────────
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
                trackColor.copy(alpha = if (isNight) 0.15f else 0.3f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )

            // ─ Traveled arc ──────────────────────────────────────────
            val traveledPath = Path()
            val travelSteps = (dayProgress * steps).toInt()
            for (i in 0..travelSteps) {
                val t = i.toFloat() / steps
                val angle = PI * (1 - t)
                val x = arcPadding + arcWidth * t
                val y = horizonY - arcHeight * sin(angle).toFloat()
                if (i == 0) traveledPath.moveTo(x, y) else traveledPath.lineTo(x, y)
            }
            drawPath(traveledPath, accentColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // ─ Celestial body position ───────────────────────────────
            val bodyAngle = PI * (1 - dayProgress)
            val bodyX = arcPadding + arcWidth * dayProgress
            val bodyY = horizonY - arcHeight * sin(bodyAngle).toFloat()

            if (isNight || isDusk) {
                // ═══ MOON ════════════════════════════════════════════
                // Outer glow
                drawCircle(
                    glowColor.copy(alpha = 0.10f),
                    radius = 44f,
                    center = Offset(bodyX, bodyY)
                )
                drawCircle(
                    glowColor.copy(alpha = 0.18f),
                    radius = 32f,
                    center = Offset(bodyX, bodyY)
                )
                drawCircle(
                    glowColor.copy(alpha = 0.28f),
                    radius = 22f,
                    center = Offset(bodyX, bodyY)
                )

                // Moon body
                drawCircle(bodyColor, radius = 16f, center = Offset(bodyX, bodyY))

                // Crescent shadow (makes it look like a crescent moon)
                drawCircle(
                    color = if (isNight) prayerColors.nightSkyStart.copy(alpha = 0.7f)
                    else prayerColors.twilightSkyStart.copy(alpha = 0.6f),
                    radius = 13f,
                    center = Offset(bodyX + 7f, bodyY - 4f),
                )

                // Craters
                drawCircle(craterColor, radius = 2.5f, center = Offset(bodyX - 5f, bodyY + 2f))
                drawCircle(craterColor, radius = 1.8f, center = Offset(bodyX - 2f, bodyY - 6f))
                drawCircle(craterColor, radius = 1.5f, center = Offset(bodyX + 1f, bodyY + 5f))

                // Highlight
                drawCircle(
                    Color.White.copy(alpha = 0.18f),
                    radius = 5f,
                    center = Offset(bodyX - 6f, bodyY - 5f)
                )
            } else {
                // ═══ SUN ═════════════════════════════════════════════
                // Glow rings
                val glowAlpha = if (isDawn) 0.5f else 1f
                drawCircle(
                    glowColor.copy(alpha = 0.08f * glowAlpha),
                    radius = 44f,
                    center = Offset(bodyX, bodyY)
                )
                drawCircle(
                    glowColor.copy(alpha = 0.15f * glowAlpha),
                    radius = 32f,
                    center = Offset(bodyX, bodyY)
                )
                drawCircle(
                    glowColor.copy(alpha = 0.30f * glowAlpha),
                    radius = 22f,
                    center = Offset(bodyX, bodyY)
                )

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
                        bodyColor.copy(alpha = rayAlpha),
                        Offset(x1, y1), Offset(x2, y2),
                        strokeWidth = 2f, cap = StrokeCap.Round,
                    )
                }

                // Sun body
                drawCircle(bodyColor, radius = 14f, center = Offset(bodyX, bodyY))
                // Highlight
                drawCircle(
                    Color.White.copy(alpha = 0.35f),
                    radius = 6f,
                    center = Offset(bodyX - 3f, bodyY - 3f)
                )
            }

            // Endpoint dots
            drawCircle(trackColor, radius = 4f, center = Offset(arcPadding, horizonY))
            drawCircle(trackColor, radius = 4f, center = Offset(arcPadding + arcWidth, horizonY))
        }

        Spacer(Modifier.height(ComposaSpacing.Small))
    }
}

/** Linearly interpolate between two colours by [fraction] (0→a, 1→b). */
private fun lerpColor(a: Color, b: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f,
    )
}