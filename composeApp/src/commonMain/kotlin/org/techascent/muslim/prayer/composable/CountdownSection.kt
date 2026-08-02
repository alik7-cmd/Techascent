package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_remaining_time
import apphub.composeapp.generated.resources.text_suhur
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.localizeDigits
import org.techascent.muslim.prayer.uimodel.IftarTimeUiModel
import org.techascent.muslim.prayer.uimodel.PrayerTimeIntervalModel
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.shared.data.common.formatDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun CountdownSection(uiModel: PrayerTimeUiModel) {
    val hasWaqt = uiModel.currentPrayer?.startTimeInstant != null
            && uiModel.currentPrayer.endTimeInstant != null
    val hasFasting = uiModel.iftarTime != null
            && (uiModel.iftarTime.iftarInstant != null || uiModel.iftarTime.sahriInstant != null)

    if (!hasWaqt && !hasFasting) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        if (hasWaqt) {
            Box(modifier = Modifier.weight(1f)) {
                WaqtCountdown(uiModel.currentPrayer!!)
            }
        }
        if (hasFasting) {
            Box(modifier = Modifier.weight(1f)) {
                FastingCountdown(uiModel.iftarTime!!)
            }
        }    }
}

// ─── Waqt (prayer time) countdown ───────────────────────────────────────────────

@Composable
private fun WaqtCountdown(prayer: PrayerTimeIntervalModel) {
    val end = prayer.endTimeInstant!!
    val total = (end - prayer.startTimeInstant!!).coerceAtLeast(Duration.ZERO)
    val accent = ComposaTheme.color.prayer.timerAccent
    val track = ComposaTheme.color.prayer.timerTrack

    var remaining by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect(end) {
        while (true) {
            val d = end - Clock.System.now()
            remaining = if (d.isPositive()) d else Duration.ZERO
            delay(1000.milliseconds)
        }
    }

    val progress = remember(remaining) {
        (1f - remaining.inWholeMilliseconds.toFloat() / total.inWholeMilliseconds.toFloat())
            .coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 6.dp.toPx()
                val arcSize = Size(size.width - sw, size.height - sw)
                val tl = Offset(sw / 2, sw / 2)
                drawArc(
                    color = track, startAngle = 0f, sweepAngle = 360f,
                    useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw),
                )
                drawArc(
                    color = accent, startAngle = -90f, sweepAngle = 360f * progress,
                    useCenter = false, topLeft = tl, size = arcSize,
                    style = Stroke(sw, cap = StrokeCap.Round),
                )
            }
            Text("⏱️", fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = remaining.formatDuration().localizeDigits(),
            style = ComposaTheme.typography.bodyEmphasized,
            color = accent,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${stringResource(Res.string.text_remaining_time)} ${stringResource(prayer.name.toDisplayString())}",
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Fasting (Sahri / Iftar) countdown ─────────────────────────────────────────

@Composable
private fun FastingCountdown(iftar: IftarTimeUiModel) {
    val accent = ComposaTheme.color.prayer.fastingAccent
    val iftarInstant = iftar.iftarInstant
    val sahriInstant = iftar.sahriInstant
    val iftarLabel = stringResource(Res.string.text_iftar)
    val sahriLabel = stringResource(Res.string.text_suhur)

    data class Target(val instant: Instant?, val label: String, val emoji: String)

    fun resolveTarget(now: Instant): Target = when {
        iftarInstant != null && iftarInstant > now -> Target(iftarInstant, iftarLabel, "🍽️")
        sahriInstant != null && sahriInstant > now -> Target(sahriInstant, sahriLabel, "🥣")
        else -> Target(null, iftarLabel, "🍽️")
    }

    var target by remember { mutableStateOf(resolveTarget(Clock.System.now())) }
    var remaining by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(iftarInstant, sahriInstant) {
        while (true) {
            val now = Clock.System.now()
            val resolved = resolveTarget(now)
            target = resolved
            if (resolved.instant == null) {
                remaining = Duration.ZERO
                break
            }
            val d = resolved.instant - now
            remaining = if (d.isPositive()) d else Duration.ZERO
            delay(1000.milliseconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(target.emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.height(8.dp))
        if (target.instant != null && remaining > Duration.ZERO) {
            Text(
                text = remaining.formatDuration().localizeDigits(),
                style = ComposaTheme.typography.bodyEmphasized,
                color = accent,
            )
        } else {
            Text("—", style = ComposaTheme.typography.bodyEmphasized, color = accent)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = target.label,
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
        )
    }
}


