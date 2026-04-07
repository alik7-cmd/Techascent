package org.techascent.muslim.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_calendar_empty_subtitle
import apphub.composeapp.generated.resources.text_calendar_empty_title
import apphub.composeapp.generated.resources.text_calendar_error
import apphub.composeapp.generated.resources.text_calendar_fasting
import apphub.composeapp.generated.resources.text_calendar_loading
import apphub.composeapp.generated.resources.text_calendar_no_data_for_date
import apphub.composeapp.generated.resources.text_calendar_prayer_times
import apphub.composeapp.generated.resources.text_calendar_sahri
import apphub.composeapp.generated.resources.text_calendar_sun
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_sunrise
import apphub.composeapp.generated.resources.text_sunset
import apphub.composeapp.generated.resources.title_calendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.calendar.state.CalendarUiState
import org.techascent.muslim.common.localizeDigits
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeIntervalModel
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.shared.data.common.currentDate
import org.techascent.shared.data.common.lengthOfMonth
import org.techascent.shared.data.common.toDayMonthYearString
import org.techascent.shared.data.common.yearMonth

// ─── Entry point ────────────────────────────────────────────────────────────────

@OptIn(KoinExperimentalAPI::class)
@Composable
fun PrayerCalendarView(
    onNavigateBack: () -> Unit,
) {
    ComposaTheme {
        val viewModel = koinViewModel<CalendarViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        PrayerCalendarScreen(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onPreviousMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth,
            onSelectDate = viewModel::onSelectDate,
        )
    }
}

// ─── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerCalendarScreen(
    uiState: CalendarUiState,
    onNavigateBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize().background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_calendar),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is CalendarUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.text_calendar_loading),
                        style = ComposaTheme.typography.subhead,
                        color = ComposaTheme.color.textNeutralSubtle,
                    )
                }
            }

            is CalendarUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.text_calendar_error),
                            style = ComposaTheme.typography.subheadEmphasized,
                            color = ComposaTheme.color.textNeutral,
                        )
                    }
                }
            }

            is CalendarUiState.Success -> {
                if (uiState.prayerDataMap.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📅", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(Res.string.text_calendar_empty_title),
                                style = ComposaTheme.typography.subheadEmphasized,
                                color = ComposaTheme.color.textNeutral,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.text_calendar_empty_subtitle),
                                style = ComposaTheme.typography.footnote,
                                color = ComposaTheme.color.textNeutralSubtle,
                            )
                        }
                    }
                } else {
                    CalendarSuccessContent(
                        uiState = uiState,
                        innerPadding = innerPadding,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onSelectDate = onSelectDate,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSuccessContent(
    uiState: CalendarUiState.Success,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        item {
            CalendarCard(
                displayedYear = uiState.displayedYear,
                displayedMonth = uiState.displayedMonth,
                selectedDateKey = uiState.selectedDateKey,
                prayerDataMap = uiState.prayerDataMap,
                minDate = uiState.minDate,
                maxDate = uiState.maxDate,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectDate = onSelectDate,
            )
        }

        item {
            SelectedDayCard(
                selectedDateKey = uiState.selectedDateKey,
                data = uiState.selectedDayData,
            )
        }
    }
}

// ─── Calendar Card ──────────────────────────────────────────────────────────────

@Composable
private fun CalendarCard(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDateKey: String?,
    prayerDataMap: Map<String, PrayerTimeUiModel>,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
) {
    val todayKey = remember { currentDate.toDayMonthYearString() }

    val canGoPrev = minDate != null &&
            LocalDate(displayedYear, displayedMonth, 1) > LocalDate(minDate.year, minDate.monthNumber, 1)
    val canGoNext = maxDate != null &&
            LocalDate(displayedYear, displayedMonth, 1) < LocalDate(maxDate.year, maxDate.monthNumber, 1)

    val monthName = Month(displayedMonth).name.lowercase().replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f))
            .padding(ComposaSpacing.Medium),
    ) {
        // ── Month/Year header with arrows ────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPreviousMonth, enabled = canGoPrev) {
                Text(
                    text = "‹",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canGoPrev) ComposaTheme.color.textNeutral
                    else ComposaTheme.color.textNeutralSubtle.copy(alpha = 0.3f),
                )
            }

            AnimatedContent(
                targetState = "$monthName $displayedYear",
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "monthTitle",
            ) { title ->
                Text(
                    text = title.localizeDigits(),
                    style = ComposaTheme.typography.titleDemi,
                    color = ComposaTheme.color.textNeutral,
                    textAlign = TextAlign.Center,
                )
            }

            IconButton(onClick = onNextMonth, enabled = canGoNext) {
                Text(
                    text = "›",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canGoNext) ComposaTheme.color.textNeutral
                    else ComposaTheme.color.textNeutralSubtle.copy(alpha = 0.3f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Day-of-week headers ──────────────────────────────────────────
        Row(Modifier.fillMaxWidth()) {
            listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { dow ->
                Text(
                    text = dow,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = ComposaTheme.typography.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = if (dow == "Fr") Color(0xFF1565C0) else ComposaTheme.color.textNeutralSubtle,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Day cells ────────────────────────────────────────────────────
        val firstDayOfMonth = LocalDate(displayedYear, displayedMonth, 1)
        val startOffset = firstDayOfMonth.dayOfWeek.ordinal // Mon=0, Sun=6
        val daysInMonth = yearMonth(displayedYear, displayedMonth).lengthOfMonth()

        val cells = buildList {
            repeat(startOffset) { add(null) }
            for (day in 1..daysInMonth) add(day)
        }

        val rows = cells.chunked(7)
        rows.forEach { rowCells ->
            Row(Modifier.fillMaxWidth()) {
                rowCells.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day != null) {
                            val dateKey = LocalDate(displayedYear, displayedMonth, day)
                                .toDayMonthYearString()
                            val hasData = prayerDataMap.containsKey(dateKey)
                            val isSelected = dateKey == selectedDateKey
                            val isToday = dateKey == todayKey

                            val bgColor = when {
                                isSelected -> ComposaTheme.color.backgroundAction
                                isToday -> ComposaTheme.color.backgroundAction.copy(alpha = 0.15f)
                                else -> Color.Transparent
                            }
                            val textColor = when {
                                isSelected -> Color.White
                                !hasData -> ComposaTheme.color.textNeutralSubtle.copy(alpha = 0.35f)
                                isToday -> ComposaTheme.color.backgroundAction
                                else -> ComposaTheme.color.textNeutral
                            }

                            Column(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .then(
                                        if (hasData) Modifier.clickable { onSelectDate(dateKey) }
                                        else Modifier
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = day.toString().localizeDigits(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                )
                                if (hasData && !isSelected) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(ComposaTheme.color.backgroundAction)
                                    )
                                }
                            }
                        }
                    }
                }
                val remaining = 7 - rowCells.size
                repeat(remaining) {
                    Box(Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

// ─── Selected Day Card ──────────────────────────────────────────────────────────

@Composable
private fun SelectedDayCard(
    selectedDateKey: String?,
    data: PrayerTimeUiModel?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        if (data == null) {
            if (selectedDateKey != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f))
                        .padding(ComposaSpacing.Large),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.text_calendar_no_data_for_date),
                        style = ComposaTheme.typography.subhead,
                        color = ComposaTheme.color.textNeutralSubtle,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return@Column
        }

        // ── Date header ──────────────────────────────────────────────────
        DateHeaderCard(selectedDateKey = selectedDateKey, hijriDate = data.hijriDate)

        // ── Prayer Times card ────────────────────────────────────────────
        val filteredIntervals = data.intervals.filter { it.name != PrayerNameEnum.SALAT_UD_DUHA }
        PrayerTimesCard(intervals = filteredIntervals)

        // ── Fasting card (Sahri / Iftar) ─────────────────────────────────
        data.iftarTime?.let { iftar ->
            if (iftar.lastTimeOfSahri != null || iftar.iftarStartTime != null) {
                FastingCard(sahri = iftar.lastTimeOfSahri, iftar = iftar.iftarStartTime)
            }
        }

        // ── Sun card (Sunrise / Sunset) ──────────────────────────────────
        SunCard(sunrise = data.sunrise, sunset = data.sunset)
    }
}

// ─── Date Header Card ───────────────────────────────────────────────────────────

@Composable
private fun DateHeaderCard(selectedDateKey: String?, hijriDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.backgroundAction.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ComposaTheme.color.backgroundAction.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🕌", fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (selectedDateKey ?: "").localizeDigits(),
                style = ComposaTheme.typography.titleDemi,
                color = ComposaTheme.color.textNeutral,
            )
            hijriDate.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it.localizeDigits(),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
            }
        }
    }
}

// ─── Prayer Times Card ──────────────────────────────────────────────────────────

@Composable
private fun PrayerTimesCard(intervals: List<PrayerTimeIntervalModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f)),
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ComposaSpacing.Medium, end = ComposaSpacing.Medium, top = ComposaSpacing.Medium, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🕋", fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(Res.string.text_calendar_prayer_times),
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
        )

        // Prayer rows
        intervals.forEachIndexed { index, interval ->
            PrayerRow(interval)
            if (index < intervals.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                    thickness = 0.25.dp,
                    color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.25f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

// ─── Fasting Card ───────────────────────────────────────────────────────────────

@Composable
private fun FastingCard(sahri: String?, iftar: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF4A148C).copy(alpha = 0.06f)),
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ComposaSpacing.Medium, end = ComposaSpacing.Medium, top = ComposaSpacing.Medium, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A148C).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🌙", fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(Res.string.text_calendar_fasting),
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = Color(0xFF4A148C).copy(alpha = 0.10f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            sahri?.let {
                FastingTimeChip(
                    emoji = "🍽️",
                    label = stringResource(Res.string.text_calendar_sahri),
                    value = it,
                    accentColor = Color(0xFF4A148C),
                )
            }
            iftar?.let {
                FastingTimeChip(
                    emoji = "🌅",
                    label = stringResource(Res.string.text_iftar),
                    value = it,
                    accentColor = Color(0xFFE65100),
                )
            }
        }
    }
}

@Composable
private fun FastingTimeChip(
    emoji: String,
    label: String,
    value: String,
    accentColor: Color,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value.localizeDigits(),
            style = ComposaTheme.typography.titleDemi,
            color = ComposaTheme.color.textNeutral,
        )
    }
}

// ─── Sun Card ───────────────────────────────────────────────────────────────────

@Composable
private fun SunCard(sunrise: String, sunset: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF57F17).copy(alpha = 0.06f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ComposaSpacing.Medium, end = ComposaSpacing.Medium, top = ComposaSpacing.Medium, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF57F17).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("☀️", fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(Res.string.text_calendar_sun),
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = Color(0xFFF57F17).copy(alpha = 0.10f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SunTimeChip(emoji = "🌅", label = stringResource(Res.string.text_sunrise), value = sunrise)
            SunTimeChip(emoji = "🌇", label = stringResource(Res.string.text_sunset), value = sunset)
        }
    }
}

@Composable
private fun SunTimeChip(emoji: String, label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF57F17).copy(alpha = 0.06f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, style = ComposaTheme.typography.caption, color = ComposaTheme.color.textNeutralSubtle)
        Spacer(Modifier.height(2.dp))
        Text(value.localizeDigits(), style = ComposaTheme.typography.titleDemi, color = ComposaTheme.color.textNeutral)
    }
}

// ─── Prayer Row ─────────────────────────────────────────────────────────────────

private fun prayerEmoji(name: PrayerNameEnum): String = when (name) {
    PrayerNameEnum.FAJR -> "🌙"
    PrayerNameEnum.SALAT_UD_DUHA -> "☀️"
    PrayerNameEnum.DUHR -> "🌤️"
    PrayerNameEnum.ASR -> "⛅"
    PrayerNameEnum.MAGHRIB -> "🌅"
    PrayerNameEnum.ISHA -> "🌑"
}

@Composable
private fun PrayerRow(interval: PrayerTimeIntervalModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = prayerEmoji(interval.name), fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(interval.name.toDisplayString()),
            style = ComposaTheme.typography.subhead,
            color = ComposaTheme.color.textNeutral,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = interval.displayableStartTime.localizeDigits(),
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutral,
        )
        Text(
            text = "  –  ",
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
        )
        Text(
            text = interval.displayableEndTime.localizeDigits(),
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutralSubtle,
        )
    }
}

