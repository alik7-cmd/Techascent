package org.techascent.muslim.prayer.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum

// ═════════════════════════════════════════════════════════════════════════════════
//  PUBLIC ENTRY — orchestrates the screen, delegates all UI to component files
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
internal fun PrayerContentV3(
    uiState: PrayerTimeUiState,
    onFetchPrayers: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
    innerPadding: PaddingValues,
) {
    val listState = rememberLazyListState()
    val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }
    val arrowAlpha by animateFloatAsState(
        targetValue = if (showScrollIndicator) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "scrollIndicatorAlpha",
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
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
                    is PrayerTimeUiState.Success -> prayerBody(
                        uiState = uiState,
                        onUpdateNotification = onUpdateNotification,
                    )
                    is PrayerTimeUiState.SuccessWithWarning -> {
                        item { LocationWarningBanner(cityName = uiState.cityName) }
                        prayerBody(
                            uiState = uiState,
                            onUpdateNotification = onUpdateNotification,
                        )
                    }
                    is PrayerTimeUiState.Error -> errorContent(onRetry = onFetchPrayers)
                }
            }

            val isSuccess = uiState is PrayerTimeUiState.Success
                    || uiState is PrayerTimeUiState.SuccessWithWarning
            if (arrowAlpha > 0f && isSuccess) {
                ScrollDownIndicator(
                    alpha = arrowAlpha,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

// ─── Private body builder ────────────────────────────────────────────────────────

private fun LazyListScope.prayerBody(
    uiState: PrayerTimeUiState,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    val uiModel = when (uiState) {
        is PrayerTimeUiState.Success -> uiState.data
        is PrayerTimeUiState.SuccessWithWarning -> uiState.data
        else -> return
    }

    item { SunProgressSection(uiModel) }
    item { CountdownSection(uiModel) }
    item { PrayerTimesSection(uiModel, onUpdateNotification) }

    uiModel.iftarTime?.let { iftar ->
        if (iftar.lastTimeOfSahri != null || iftar.iftarStartTime != null) {
            item { FastingSection(sahri = iftar.lastTimeOfSahri, iftar = iftar.iftarStartTime) }
        }
    }

    item { AnnouncementSection() }
}
