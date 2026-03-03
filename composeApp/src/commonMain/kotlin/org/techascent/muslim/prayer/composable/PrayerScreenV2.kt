package org.techascent.muslim.prayer.composable

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.ic_notification_off
import apphub.composeapp.generated.resources.ic_notification_on
import apphub.composeapp.generated.resources.img_asr
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_halal_scan_action
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_notification_permission_description
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_prayer_all_times
import apphub.composeapp.generated.resources.text_prayer_current_waqt
import apphub.composeapp.generated.resources.text_prayer_fasting
import apphub.composeapp.generated.resources.text_remaining_time
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.text_suhur
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
import org.techascent.muslim.prayer.uimodel.toDisplayImageRes
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog
import kotlin.time.Duration

// ─── Accent colours ─────────────────────────────────────────────────────────────
private val PrayerBlue = Color(0xFF1565C0)
private val FastingAmber = Color(0xFFE65100)
private val HalalGreen = Color(0xFF4CAF50)
private val TimerTeal = Color(0xFF00838F)

// ═════════════════════════════════════════════════════════════════════════════════
//  PUBLIC ENTRY POINT
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
internal fun PrayerContentV2(
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
            // ── Greeting + halal pill ───────────────────────────────────
            item { GreetingRow(onNavigateHalalScanner = onNavigateHalalScanner) }

            // ── Main body ───────────────────────────────────────────────
            when (uiState) {
                is PrayerTimeUiState.Loading -> loadingContent()
                is PrayerTimeUiState.Success -> prayerBody(
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
            text = stringResource(Res.string.text_utility_greeting),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
            modifier = Modifier.weight(1f),
        )
        HalalPill(onNavigateHalalScanner)
    }
}

@Composable
private fun HalalPill(onNavigateHalalScanner: () -> Unit) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    val pTitle = stringResource(Res.string.text_permission_title)
    val pMsg = stringResource(Res.string.text_permission_description)
    val pOk = stringResource(Res.string.button_open_settings)
    val pCancel = stringResource(Res.string.text_cancel)
    BindEffect(ctrl)

    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(
        if (pressed) 0.93f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pill",
    )

    Row(
        modifier = Modifier
            .scale(s)
            .clip(RoundedCornerShape(24.dp))
            .background(HalalGreen.copy(alpha = 0.10f))
            .clickable(interactionSource = src, indication = null) {
                scope.launch {
                    try {
                        ctrl.providePermission(Permission.CAMERA)
                        onNavigateHalalScanner()
                    } catch (_: DeniedException) {
                        showPermissionRationalDialog(pTitle, pMsg, pOk, pCancel, { ctrl.openAppSettings() })
                    } catch (_: DeniedAlwaysException) {
                        showPermissionRationalDialog(pTitle, pMsg, pOk, pCancel, { ctrl.openAppSettings() })
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(HalalGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Text("🔍", fontSize = 14.sp) }
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.text_halal_scan_action),
            style = ComposaTheme.typography.captionEmphasized,
            color = HalalGreen,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  PRAYER BODY (success state)
// ═════════════════════════════════════════════════════════════════════════════════

private fun LazyListScope.prayerBody(
    uiModel: PrayerTimeUiModel,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    // 1 — Hero card
    item { HeroCard(uiModel) }

    // 2 — Countdown timer
    if (uiModel.currentPrayer?.startTimeInstant != null &&
        uiModel.currentPrayer.endTimeInstant != null
    ) {
        item { CountdownCard(uiModel.currentPrayer) }
    }

    // 3 — School info banner
    item {
        val schoolText = stringResource(uiModel.school.toTextRes())
        MessageBox(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            messageType = MessageType.Info,
            message = stringResource(Res.string.warning_prayer_time, schoolText),
        )
    }

    // 4 — All prayer times
    item {
        PrayerTimesSection(
            uiModel = uiModel,
            onUpdateNotification = onUpdateNotification,
        )
    }

    // 5 — Fasting (iftar / suhur)
    uiModel.iftarTime?.let { iftar ->
        item { FastingSection(iftar) }
    }

    // 6 — Halal scanner promo
    //featureCard(onClick = onNavigateHalalScanner)

    item { Spacer(Modifier.height(ComposaSpacing.Small)) }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  1. HERO CARD
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroCard(uiModel: PrayerTimeUiModel) {
    val prayer = uiModel.currentPrayer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        Image(
            painter = painterResource(prayer?.name?.toDisplayImageRes() ?: Res.drawable.img_asr),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(2.1f),
        )
        // scrim
        Box(
            Modifier.fillMaxWidth().aspectRatio(2.1f)
                .background(Color.Black.copy(alpha = 0.35f))
        )
        // overlay text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.1f)
                .padding(ComposaSpacing.Medium),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // top — location & dates
            Column {
                Text(
                    text = uiModel.addressInfo.district?.plus(", ${uiModel.addressInfo.country}")
                        ?: uiModel.addressInfo.address,
                    style = ComposaTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${uiModel.currentDateTime}  •  ${uiModel.hijriDate}",
                    style = ComposaTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            // bottom — prayer name + sunrise/sunset
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    prayer?.name?.let {
                        Text(
                            stringResource(it.toDisplayString()),
                            style = ComposaTheme.typography.titleMediumEmphasized,
                            color = Color.White,
                        )
                    }
                    prayer?.let {
                        Text(
                            "${it.displayableStartTime} – ${it.displayableEndTime}",
                            style = ComposaTheme.typography.subhead,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌅", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(uiModel.sunrise, style = ComposaTheme.typography.caption, color = Color.White.copy(0.85f))
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌇", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(uiModel.sunset, style = ComposaTheme.typography.caption, color = Color.White.copy(0.85f))
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  2. COUNTDOWN CARD
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun CountdownCard(prayer: PrayerTimeIntervalModel) {
    val end = prayer.endTimeInstant!!
    val total = (end - prayer.startTimeInstant!!).coerceAtLeast(Duration.ZERO)

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(TimerTeal.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 6.dp.toPx()
                val arcSize = Size(size.width - sw, size.height - sw)
                val tl = Offset(sw / 2, sw / 2)
                drawArc(color = TimerTeal.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw))
                drawArc(color = TimerTeal, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, topLeft = tl, size = arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            }
            Text("⏱️", fontSize = 20.sp)
        }
        Spacer(Modifier.width(ComposaSpacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${stringResource(Res.string.text_remaining_time)} ${stringResource(prayer.name.toDisplayString())}",
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                remaining.formatDuration(),
                style = ComposaTheme.typography.titleEmphasized,
                color = TimerTeal,
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  4. PRAYER TIMES SECTION
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrayerTimesSection(
    uiModel: PrayerTimeUiModel,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    BindEffect(ctrl)
    val pTitle = stringResource(Res.string.text_permission_title)
    val pMsg = stringResource(Res.string.text_notification_permission_description)
    val pOk = stringResource(Res.string.button_open_settings)
    val pCancel = stringResource(Res.string.text_cancel)

    SectionCard(
        emoji = "🕌",
        title = stringResource(Res.string.text_prayer_all_times),
        accentColor = PrayerBlue,
    ) {
        val visibleIntervals = uiModel.intervals.filter {
            it.name.toDisplayString() != Res.string.text_salat_ud_duha
        }
        visibleIntervals.forEachIndexed { index, interval ->
            val isCurrent = interval.displayableStartTime == uiModel.currentPrayer?.displayableStartTime
            var notify by remember { mutableStateOf(interval.shouldNotify) }

            PrayerRow(
                name = stringResource(interval.name.toDisplayString()),
                time = interval.displayableStartTime,
                endTime = interval.displayableEndTime,
                isCurrent = isCurrent,
                shouldNotify = notify,
                emoji = interval.name.toEmoji(),
                onClick = {
                    scope.launch {
                        try {
                            ctrl.providePermission(Permission.REMOTE_NOTIFICATION)
                            notify = !notify
                            onUpdateNotification(notify, interval.name)
                        } catch (_: DeniedException) {
                            showPermissionRationalDialog(pTitle, pMsg, pOk, pCancel, { ctrl.openAppSettings() })
                        } catch (_: DeniedAlwaysException) {
                            showPermissionRationalDialog(pTitle, pMsg, pOk, pCancel, { ctrl.openAppSettings() })
                        }
                    }
                },
            )

            if (index < visibleIntervals.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                    thickness = 0.5.dp,
                    color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.3f),
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    name: String,
    time: String,
    endTime: String,
    isCurrent: Boolean,
    shouldNotify: Boolean,
    emoji: String,
    onClick: () -> Unit,
) {
    val bg = if (isCurrent) PrayerBlue.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) PrayerBlue.copy(alpha = 0.14f)
                    else ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 16.sp) }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = if (isCurrent) ComposaTheme.typography.subheadEmphasized else ComposaTheme.typography.subhead,
                color = if (isCurrent) PrayerBlue else ComposaTheme.color.textNeutral,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "$time – $endTime",
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
            )
        }

        if (isCurrent) {
            Text(
                stringResource(Res.string.text_prayer_current_waqt),
                style = ComposaTheme.typography.captionEmphasized,
                color = PrayerBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrayerBlue.copy(alpha = 0.10f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(8.dp))
        }

        val iconRes = if (shouldNotify) Res.drawable.ic_notification_on else Res.drawable.ic_notification_off
        val tint = if (shouldNotify) PrayerBlue else ComposaTheme.color.textNeutralSubtle
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  5. FASTING SECTION
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun FastingSection(iftar: IftarTimeUiModel) {
    SectionCard(
        emoji = "🌙",
        title = stringResource(Res.string.text_prayer_fasting),
        accentColor = FastingAmber,
    ) {
        FastingRow("🍽️", stringResource(Res.string.text_iftar), iftar.iftarStartTime ?: "—")
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.3f),
        )
        FastingRow("🥣", stringResource(Res.string.text_suhur), iftar.lastTimeOfSahri ?: "—")
    }
}

@Composable
private fun FastingRow(emoji: String, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(label, style = ComposaTheme.typography.subhead, color = ComposaTheme.color.textNeutral, modifier = Modifier.weight(1f))
        Text(value, style = ComposaTheme.typography.subheadEmphasized, color = FastingAmber)
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  SHARED SECTION CARD  (same pattern as SettingsScreenV2)
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    emoji: String,
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(ComposaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Text(title, style = ComposaTheme.typography.subheadEmphasized, color = ComposaTheme.color.textNeutral)
        }
        HorizontalDivider(
            Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
        )
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

