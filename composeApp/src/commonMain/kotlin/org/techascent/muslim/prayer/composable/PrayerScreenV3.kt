package org.techascent.muslim.prayer.composable

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.ic_notification_off
import apphub.composeapp.generated.resources.ic_notification_on
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_halal_scan_action
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_notification_permission_description
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_prayer_all_times
import apphub.composeapp.generated.resources.text_prayer_announcement
import apphub.composeapp.generated.resources.text_prayer_current_waqt
import apphub.composeapp.generated.resources.text_prayer_fasting
import apphub.composeapp.generated.resources.text_prayer_no_announcement
import apphub.composeapp.generated.resources.text_remaining_time
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.text_suhur
import apphub.composeapp.generated.resources.text_sunrise
import apphub.composeapp.generated.resources.text_sunset
import apphub.composeapp.generated.resources.text_utility_greeting
import apphub.composeapp.generated.resources.warning_prayer_time
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.formatDuration
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.IftarTimeUiModel
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeIntervalModel
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration

// ═════════════════════════════════════════════════════════════════════════════════
//  PUBLIC ENTRY
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
internal fun PrayerContentV3(
    uiState: PrayerTimeUiState,
    onFetchPrayers: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
    innerPadding: PaddingValues,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + ComposaSpacing.ExtraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            item { GreetingRow(onNavigateHalalScanner) }

            when (uiState) {
                is PrayerTimeUiState.Loading -> loadingContent()
                is PrayerTimeUiState.Success -> prayerBodyV3(
                    uiModel = uiState.data,
                    onNavigateHalalScanner = onNavigateHalalScanner,
                    onUpdateNotification = onUpdateNotification,
                )
                is PrayerTimeUiState.Error -> errorContent(onRetry = onFetchPrayers)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  GREETING ROW
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun GreetingRow(onNavigateHalalScanner: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .padding(top = ComposaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.text_utility_greeting),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
            modifier = Modifier.weight(1f),
        )
        HalalPill(onNavigateHalalScanner)
    }
}

@Composable
private fun HalalPill(onClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    val pT = stringResource(Res.string.text_permission_title)
    val pM = stringResource(Res.string.text_permission_description)
    val pO = stringResource(Res.string.button_open_settings)
    val pC = stringResource(Res.string.text_cancel)
    BindEffect(ctrl)

    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(
        if (pressed) 0.93f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pill",
    )
    val accent = ComposaTheme.color.prayer.scannerAccent
    val subtle = ComposaTheme.color.prayer.scannerSubtle

    Row(
        modifier = Modifier
            .scale(s)
            .clip(RoundedCornerShape(24.dp))
            .background(subtle)
            .clickable(interactionSource = src, indication = null) {
                scope.launch {
                    try { ctrl.providePermission(Permission.CAMERA); onClick() }
                    catch (_: DeniedException) { showPermissionRationalDialog(pT, pM, pO, pC, { ctrl.openAppSettings() }) }
                    catch (_: DeniedAlwaysException) { showPermissionRationalDialog(pT, pM, pO, pC, { ctrl.openAppSettings() }) }
                }
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Text("🔍", fontSize = 14.sp) }
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.text_halal_scan_action),
            style = ComposaTheme.typography.captionEmphasized, color = accent,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  BODY
// ═════════════════════════════════════════════════════════════════════════════════

private fun LazyListScope.prayerBodyV3(
    uiModel: PrayerTimeUiModel,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    // 1 — Sun progress hero (compact)
    item { SunProgressCard(uiModel) }

    // 2 — Waqt countdown + fasting countdown side by side
    item { CountdownRow(uiModel) }

    // 3 — Prayer times + Fasting side by side
    item {
        PrayerAndFastingRow(
            uiModel = uiModel,
            onUpdateNotification = onUpdateNotification,
        )
    }

    // 4 — School info
    /*item {
        MessageBox(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            messageType = MessageType.Info,
            message = stringResource(Res.string.warning_prayer_time, stringResource(uiModel.school.toTextRes())),
        )
    }*/

    // 5 — Announcement
    item { AnnouncementSection() }

    item { Spacer(Modifier.height(ComposaSpacing.Small)) }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  1. SUN PROGRESS HERO — larger sun with rays + glow rings
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SunProgressCard(uiModel: PrayerTimeUiModel) {
    val prayer = uiModel.currentPrayer
    val skyStart = ComposaTheme.color.prayer.skyStart
    val skyEnd = ComposaTheme.color.prayer.skyEnd
    val sunBody = ComposaTheme.color.prayer.sunBody
    val sunGlow = ComposaTheme.color.prayer.sunGlow
    val horizon = ComposaTheme.color.prayer.horizon
    val timerAccent = ComposaTheme.color.prayer.timerAccent
    val timerTrack = ComposaTheme.color.prayer.timerTrack

    val intervals = uiModel.intervals
    val firstStart = intervals.firstOrNull()?.startTimeInstant
    val lastEnd = intervals.lastOrNull()?.endTimeInstant
    val now = Clock.System.now()

    val dayProgress = if (firstStart != null && lastEnd != null && lastEnd > firstStart) {
        val total = (lastEnd - firstStart).inWholeMilliseconds.toFloat()
        val elapsed = (now - firstStart).inWholeMilliseconds.toFloat()
        (elapsed / total).coerceIn(0f, 1f)
    } else 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(skyStart, skyEnd))),
    ) {
        // ── Current prayer + location info at the top ────────────────
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .padding(top = ComposaSpacing.Small),
        ) {
            // Location + dates on one line
            Text(
                buildString {
                    append(uiModel.addressInfo.district?.plus(", ${uiModel.addressInfo.country}") ?: uiModel.addressInfo.address)
                    append("  •  ")
                    append(uiModel.hijriDate)
                },
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            // Current prayer name + time inline
            prayer?.let {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(it.name.toDisplayString()),
                        style = ComposaTheme.typography.titleEmphasized,
                        color = ComposaTheme.color.textNeutral,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${it.displayableStartTime} – ${it.displayableEndTime}",
                        style = ComposaTheme.typography.footnote,
                        color = ComposaTheme.color.textNeutralSubtle,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        // ── Canvas — sun arc with rays (compact) ─────────────────────
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.85f
            val arcPadding = w * 0.08f
            val arcWidth = w - 2 * arcPadding
            val arcHeight = h * 0.72f

            // Horizon line
            drawLine(horizon, Offset(0f, horizonY), Offset(w, horizonY), strokeWidth = 1.5f)

            // Arc track (semi-ellipse)
            val arcPath = Path()
            val steps = 120
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val angle = PI * (1 - t)
                val x = arcPadding + arcWidth * t
                val y = horizonY - arcHeight * sin(angle).toFloat()
                if (i == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
            }
            drawPath(arcPath, timerTrack.copy(alpha = 0.3f), style = Stroke(width = 2f, cap = StrokeCap.Round))

            // Traveled arc
            val traveledPath = Path()
            val travelSteps = (dayProgress * steps).toInt()
            for (i in 0..travelSteps) {
                val t = i.toFloat() / steps
                val angle = PI * (1 - t)
                val x = arcPadding + arcWidth * t
                val y = horizonY - arcHeight * sin(angle).toFloat()
                if (i == 0) traveledPath.moveTo(x, y) else traveledPath.lineTo(x, y)
            }
            drawPath(traveledPath, timerAccent, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // Sun position
            val sunAngle = PI * (1 - dayProgress)
            val sunX = arcPadding + arcWidth * dayProgress
            val sunY = horizonY - arcHeight * sin(sunAngle).toFloat()

            // Outer glow rings (3 layers)
            drawCircle(sunGlow.copy(alpha = 0.08f), radius = 44f, center = Offset(sunX, sunY))
            drawCircle(sunGlow.copy(alpha = 0.15f), radius = 32f, center = Offset(sunX, sunY))
            drawCircle(sunGlow.copy(alpha = 0.30f), radius = 22f, center = Offset(sunX, sunY))

            // Sun rays (12 short lines radiating out)
            val rayCount = 12
            val innerR = 16f
            val outerR = 28f
            for (r in 0 until rayCount) {
                val rayAngle = 2.0 * PI * r / rayCount
                val x1 = sunX + innerR * cos(rayAngle).toFloat()
                val y1 = sunY + innerR * sin(rayAngle).toFloat()
                val x2 = sunX + outerR * cos(rayAngle).toFloat()
                val y2 = sunY + outerR * sin(rayAngle).toFloat()
                drawLine(
                    sunBody.copy(alpha = 0.6f),
                    Offset(x1, y1), Offset(x2, y2),
                    strokeWidth = 2f, cap = StrokeCap.Round,
                )
            }

            // Sun body
            drawCircle(sunBody, radius = 14f, center = Offset(sunX, sunY))
            // Highlight
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(sunX - 3f, sunY - 3f))

            // Endpoint dots
            drawCircle(timerTrack, radius = 4f, center = Offset(arcPadding, horizonY))
            drawCircle(timerTrack, radius = 4f, center = Offset(arcPadding + arcWidth, horizonY))
        }

        // Sunrise / Sunset labels
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .padding(bottom = ComposaSpacing.Small),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌅", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${stringResource(Res.string.text_sunrise)} ${uiModel.sunrise}",
                    style = ComposaTheme.typography.caption,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌇", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${stringResource(Res.string.text_sunset)} ${uiModel.sunset}",
                    style = ComposaTheme.typography.caption,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  2. COUNTDOWN ROW  — waqt timer (left) + fasting timer (right)
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun CountdownRow(uiModel: PrayerTimeUiModel) {
    val hasWaqt = uiModel.currentPrayer?.startTimeInstant != null && uiModel.currentPrayer.endTimeInstant != null
    val hasFasting = uiModel.iftarTime != null && (uiModel.iftarTime.iftarInstant != null || uiModel.iftarTime.sahriInstant != null)

    if (!hasWaqt && !hasFasting) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
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
        }
    }
}

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
            delay(1000)
        }
    }
    val progress = remember(remaining) {
        (1f - remaining.inWholeMilliseconds.toFloat() / total.inWholeMilliseconds.toFloat()).coerceIn(0f, 1f)
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
                drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw))
                drawArc(color = accent, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            }
            Text("⏱️", fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            remaining.formatDuration(),
            style = ComposaTheme.typography.bodyEmphasized,
            color = accent,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${stringResource(Res.string.text_remaining_time)} ${stringResource(prayer.name.toDisplayString())}",
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FastingCountdown(iftar: IftarTimeUiModel) {
    val now = Clock.System.now()
    val accent = ComposaTheme.color.prayer.fastingAccent

    // Determine which is closer: iftar or suhur
    val iftarInstant = iftar.iftarInstant
    val sahriInstant = iftar.sahriInstant

    val targetInstant: Instant?
    val targetLabel: String
    val targetEmoji: String

    if (iftarInstant != null && iftarInstant > now) {
        targetInstant = iftarInstant
        targetLabel = stringResource(Res.string.text_iftar)
        targetEmoji = "🍽️"
    } else if (sahriInstant != null && sahriInstant > now) {
        targetInstant = sahriInstant
        targetLabel = stringResource(Res.string.text_suhur)
        targetEmoji = "🥣"
    } else {
        targetInstant = null
        targetLabel = stringResource(Res.string.text_iftar)
        targetEmoji = "🍽️"
    }

    var remaining by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect(targetInstant) {
        if (targetInstant == null) return@LaunchedEffect
        while (true) {
            val d = targetInstant - Clock.System.now()
            remaining = if (d.isPositive()) d else Duration.ZERO
            delay(1000)
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
            Modifier.size(64.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) { Text(targetEmoji, fontSize = 24.sp) }
        Spacer(Modifier.height(8.dp))
        if (targetInstant != null && remaining > Duration.ZERO) {
            Text(
                remaining.formatDuration(),
                style = ComposaTheme.typography.bodyEmphasized,
                color = accent,
            )
        } else {
            Text("—", style = ComposaTheme.typography.bodyEmphasized, color = accent)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            targetLabel,
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  3. PRAYER TIMES + FASTING  — side by side
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrayerAndFastingRow(
    uiModel: PrayerTimeUiModel,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    val hasFasting = uiModel.iftarTime != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        // Prayer list — takes more space
        Box(modifier = Modifier.weight(if (hasFasting) 1.6f else 1f)) {
            PrayerTimesCard(uiModel, onUpdateNotification)
        }
        // Fasting card — compact right column
        if (hasFasting) {
            Box(modifier = Modifier.weight(1f)) {
                FastingSideCard(uiModel.iftarTime!!)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  3a. PRAYER TIMES CARD  (no outer horizontal padding — parent handles it)
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrayerTimesCard(
    uiModel: PrayerTimeUiModel,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    BindEffect(ctrl)
    val pT = stringResource(Res.string.text_permission_title)
    val pM = stringResource(Res.string.text_notification_permission_description)
    val pO = stringResource(Res.string.button_open_settings)
    val pC = stringResource(Res.string.text_cancel)

    val currentWaqtBg = ComposaTheme.color.prayer.currentWaqtBg
    val currentWaqtText = ComposaTheme.color.prayer.currentWaqtText
    val cardBg = ComposaTheme.color.prayer.cardBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(currentWaqtText.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text("🕌", fontSize = 18.sp) }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.text_prayer_all_times),
                style = ComposaTheme.typography.footnoteEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 12.dp), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.4f))

        val visible = uiModel.intervals.filter { it.name.toDisplayString() != Res.string.text_salat_ud_duha }
        visible.forEachIndexed { idx, interval ->
            val isCurrent = interval.displayableStartTime == uiModel.currentPrayer?.displayableStartTime
            var notify by remember { mutableStateOf(interval.shouldNotify) }

            CompactPrayerRow(
                name = stringResource(interval.name.toDisplayString()),
                time = interval.displayableStartTime,
                isCurrent = isCurrent,
                shouldNotify = notify,
                emoji = interval.name.toEmoji(),
                currentWaqtBg = currentWaqtBg,
                currentWaqtText = currentWaqtText,
                onClick = {
                    scope.launch {
                        try {
                            ctrl.providePermission(Permission.REMOTE_NOTIFICATION)
                            notify = !notify
                            onUpdateNotification(notify, interval.name)
                        } catch (_: DeniedException) { showPermissionRationalDialog(pT, pM, pO, pC, { ctrl.openAppSettings() }) }
                        catch (_: DeniedAlwaysException) { showPermissionRationalDialog(pT, pM, pO, pC, { ctrl.openAppSettings() }) }
                    }
                },
            )
            if (idx < visible.lastIndex) {
                HorizontalDivider(Modifier.padding(horizontal = 12.dp), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))
            }
        }
    }
}

@Composable
private fun CompactPrayerRow(
    name: String, time: String,
    isCurrent: Boolean, shouldNotify: Boolean, emoji: String,
    currentWaqtBg: Color, currentWaqtText: Color,
    onClick: () -> Unit,
) {
    val bg = if (isCurrent) currentWaqtBg.copy(alpha = 0.12f) else Color.Transparent

    Row(
        modifier = Modifier.fillMaxWidth().background(bg).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            name,
            style = if (isCurrent) ComposaTheme.typography.footnoteEmphasized else ComposaTheme.typography.footnote,
            color = if (isCurrent) currentWaqtText else ComposaTheme.color.textNeutral,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            time,
            style = ComposaTheme.typography.footnoteEmphasized,
            color = if (isCurrent) currentWaqtText else ComposaTheme.color.textNeutralSubtle,
        )
        Spacer(Modifier.width(6.dp))
        val iconRes = if (shouldNotify) Res.drawable.ic_notification_on else Res.drawable.ic_notification_off
        val tint = if (shouldNotify) currentWaqtText else ComposaTheme.color.textNeutralSubtle
        Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  3b. FASTING SIDE CARD  (compact vertical card for the right column)
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun FastingSideCard(iftar: IftarTimeUiModel) {
    val accent = ComposaTheme.color.prayer.fastingAccent
    val cardBg = ComposaTheme.color.prayer.cardBg
    val now = Clock.System.now()

    // Determine closer fasting event for mini countdown
    val iftarInstant = iftar.iftarInstant
    val sahriInstant = iftar.sahriInstant
    val closerIsIftar = iftarInstant != null && iftarInstant > now
    val closerInstant = if (closerIsIftar) iftarInstant else if (sahriInstant != null && sahriInstant > now) sahriInstant else null

    var remaining by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect(closerInstant) {
        if (closerInstant == null) return@LaunchedEffect
        while (true) {
            val d = closerInstant - Clock.System.now()
            remaining = if (d.isPositive()) d else Duration.ZERO
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(accent.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text("🌙", fontSize = 18.sp) }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.text_prayer_fasting),
                style = ComposaTheme.typography.footnoteEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 12.dp), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.4f))

        // Iftar row
        FastingCompactRow("🍽️", stringResource(Res.string.text_iftar), iftar.iftarStartTime ?: "—", accent)
        HorizontalDivider(Modifier.padding(horizontal = 12.dp), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))

        // Suhur row
        FastingCompactRow("🥣", stringResource(Res.string.text_suhur), iftar.lastTimeOfSahri ?: "—", accent)

        // Mini countdown for closer event
        if (closerInstant != null && remaining > Duration.ZERO) {
            HorizontalDivider(Modifier.padding(horizontal = 12.dp), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (closerIsIftar) "🍽️ ${stringResource(Res.string.text_iftar)}" else "🥣 ${stringResource(Res.string.text_suhur)}",
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    remaining.formatDuration(),
                    style = ComposaTheme.typography.bodyEmphasized,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun FastingCompactRow(emoji: String, label: String, value: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, style = ComposaTheme.typography.footnote, color = ComposaTheme.color.textNeutral, modifier = Modifier.weight(1f))
        Text(value, style = ComposaTheme.typography.footnoteEmphasized, color = accent)
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  4. ANNOUNCEMENT SECTION
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun AnnouncementSection() {
    val accent = ComposaTheme.color.prayer.announcementAccent

    SectionCard(emoji = "📢", title = stringResource(Res.string.text_prayer_announcement), accentColor = accent) {
        Row(
            Modifier.fillMaxWidth().padding(ComposaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("📌", fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(Res.string.text_prayer_no_announcement),
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutralSubtle,
                textAlign = TextAlign.Start,
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  SHARED SECTION CARD
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    emoji: String, title: String, accentColor: Color,
    content: @Composable () -> Unit,
) {
    val cardBg = ComposaTheme.color.prayer.cardBg
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp)).background(cardBg),
    ) {
        Row(Modifier.fillMaxWidth().padding(ComposaSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Text(title, style = ComposaTheme.typography.subheadEmphasized, color = ComposaTheme.color.textNeutral)
        }
        HorizontalDivider(Modifier.padding(horizontal = ComposaSpacing.Medium), 0.5.dp, ComposaTheme.color.strokeNeutralSubtle.copy(0.4f))
        content()
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────────

private fun PrayerNameEnum.toEmoji(): String = when (this) {
    PrayerNameEnum.FAJR -> "🌅"
    PrayerNameEnum.SALAT_UD_DUHA -> "☀️"
    PrayerNameEnum.DUHR -> "🌤️"
    PrayerNameEnum.ASR -> "⛅"
    PrayerNameEnum.MAGHRIB -> "🌇"
    PrayerNameEnum.ISHA -> "🌙"
}

