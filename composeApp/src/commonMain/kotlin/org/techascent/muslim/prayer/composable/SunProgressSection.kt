package org.techascent.muslim.prayer.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_sunrise
import apphub.composeapp.generated.resources.text_sunset
import kotlinx.datetime.Clock
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.sunprogress.SunProgressCard
import org.techascent.composa.sunprogress.SunProgressConfig
import org.techascent.composa.sunprogress.lerpColor
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.localizeDigits
import org.techascent.muslim.common.localizeTime
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.prayer.util.DayPhase
import org.techascent.muslim.prayer.util.SunPhaseCalculator
import kotlin.time.Duration.Companion.milliseconds

/**
 * Wrapper around the reusable [SunProgressCard] composable.
 *
 * All phase/progress computation is delegated to [SunPhaseCalculator] —
 * this file only handles presentation: colors, text, and animation.
 */
@Composable
internal fun SunProgressSection(uiModel: PrayerTimeUiModel) {
    val prayer = uiModel.currentPrayer
    val prayerColors = ComposaTheme.color.prayer
    val now = remember { Clock.System.now() }

    // Pure computation — no Compose calls
    val result = remember(now, uiModel) { SunPhaseCalculator.compute(uiModel, now) }
    val isNight = result.isNight
    val isDusk = result.isDusk
    val isDawn = result.isDawn

    // ── Dynamic sky gradient (color-only, based on result) ───────────────
    val skyGradient: List<Color> = when (result.phase) {
        DayPhase.DAY -> {
            val sunrise = uiModel.sunriseInstant
            val sunset = uiModel.sunsetInstant
            if (sunrise != null && sunset != null) {
                val total = (sunset - sunrise).inWholeMilliseconds.toFloat()
                val elapsed = (now - sunrise).inWholeMilliseconds.toFloat()
                val t = (elapsed / total).coerceIn(0f, 1f)
                val dimFactor = if (t > 0.8f) ((t - 0.8f) / 0.2f).coerceIn(0f, 1f) else 0f
                listOf(
                    lerpColor(prayerColors.skyStart, prayerColors.twilightSkyStart, dimFactor),
                    lerpColor(prayerColors.skyEnd, prayerColors.twilightSkyEnd, dimFactor),
                )
            } else {
                listOf(prayerColors.skyStart, prayerColors.skyEnd)
            }
        }
        DayPhase.DAWN  -> listOf(prayerColors.dawnSkyStart, prayerColors.dawnSkyEnd)
        DayPhase.DUSK  -> listOf(prayerColors.twilightSkyStart, prayerColors.twilightSkyEnd)
        DayPhase.NIGHT -> listOf(prayerColors.nightSkyStart, prayerColors.nightSkyEnd)
    }

    // Text colours adapt to dark/light sky
    val textOnSky = if (isNight || isDusk || isDawn)
        Color.White.copy(alpha = 0.9f) else ComposaTheme.color.textNeutral
    val subtleOnSky = if (isNight || isDusk || isDawn)
        Color.White.copy(alpha = 0.6f) else ComposaTheme.color.textNeutralSubtle

    // ── Sunrise/sunset labels fade in after a brief delay ───────────────
    var showSunTimes by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        showSunTimes = true
    }
    val sunTimesAlpha by animateFloatAsState(
        targetValue = if (showSunTimes) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "sunTimesAlpha",
    )
    val sunTimesOffset by animateFloatAsState(
        targetValue = if (showSunTimes) 0f else 8f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "sunTimesOffset",
    )

    // ── Delegate rendering to the reusable composa card ─────────────────
    SunProgressCard(
        config = SunProgressConfig(
            skyGradient = skyGradient,
            dayProgress = result.progress,
            isNight = isNight,
            isDusk = isDusk,
            isDawn = isDawn,
            bodyColor = if (isNight || isDusk) prayerColors.moonBody else prayerColors.sunBody,
            glowColor = if (isNight || isDusk) prayerColors.moonGlow else prayerColors.sunGlow,
            horizonColor = prayerColors.horizon,
            arcAccentColor = prayerColors.timerAccent,
            arcTrackColor = prayerColors.timerTrack,
            starColor = prayerColors.starColor,
            craterColor = prayerColors.moonCrater,
            nightSkyStartColor = prayerColors.nightSkyStart,
            twilightSkyStartColor = prayerColors.twilightSkyStart,
        ),
        headerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComposaSpacing.Medium)
                    .padding(top = ComposaSpacing.Medium),
            ) {
                prayer?.let {
                    Column {
                        Text(
                            text = stringResource(it.name.toDisplayString()),
                            style = ComposaTheme.typography.titleLarge,
                            color = textOnSky,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${it.displayableStartTime} – ${it.displayableEndTime}".localizeTime(),
                            style = ComposaTheme.typography.footnote,
                            color = subtleOnSky,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(
                            uiModel.addressInfo.district
                                ?.plus(", ${uiModel.addressInfo.country}")
                                ?: uiModel.addressInfo.address,
                        )
                        append("  •  ")
                        append(uiModel.hijriDate)
                    }.localizeDigits(),
                    style = ComposaTheme.typography.footnote,
                    color = subtleOnSky,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        footerContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComposaSpacing.Medium)
                    .graphicsLayer {
                        alpha = sunTimesAlpha
                        translationY = sunTimesOffset
                    },
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌅 ${stringResource(Res.string.text_sunrise)}",
                        style = ComposaTheme.typography.footnote,
                        color = subtleOnSky,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = uiModel.sunrise.localizeTime(),
                        style = ComposaTheme.typography.footnote,
                        color = subtleOnSky,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌇 ${stringResource(Res.string.text_sunset)}",
                        style = ComposaTheme.typography.footnote,
                        color = subtleOnSky,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = uiModel.sunset.localizeTime(),
                        style = ComposaTheme.typography.footnote,
                        color = subtleOnSky,
                    )
                }
            }
        },
    )
}

