package org.techascent.muslim.prayer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.composable.PrayerContentV3
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.utility.FeatureId

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun PrayerView(
    innerPadding: PaddingValues,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToFeature: (FeatureId) -> Unit = {},
) {
    val viewModel = koinViewModel<PrayerTimeViewModel>()
    val uriHandler = LocalUriHandler.current
    ComposaTheme {
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collect {
                handleEvent(event = it, uriHandler = uriHandler)
            }
        }

        PrayerScreen(
            onFetchPrayers = viewModel::getMonthlyPrayerTimes,
            innerPadding = innerPadding,
            onNavigateHalalScanner = onNavigateHalalScanner,
            onNavigateToFeature = onNavigateToFeature,
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun PrayerScreen(
    viewModel: PrayerTimeViewModel = koinViewModel<PrayerTimeViewModel>(),
    onFetchPrayers: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToFeature: (FeatureId) -> Unit,
    innerPadding: PaddingValues,
) {
    val uiState by viewModel.uiState.collectAsState()

    PrayerContentV3(
        uiState = uiState,
        onFetchPrayers = onFetchPrayers,
        onNavigateHalalScanner = onNavigateHalalScanner,
        onUpdateNotification = viewModel::onUpdateNotification,
        innerPadding = innerPadding,
        onNavigateToFeature = onNavigateToFeature,
    )

}

private fun handleEvent(event: PrayerTimeEvent, uriHandler: UriHandler) {
    when (event) {
        is PrayerTimeEvent.OpenExternalLink -> uriHandler.openUri(event.url)
    }
}