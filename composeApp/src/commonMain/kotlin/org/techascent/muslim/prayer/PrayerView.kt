package org.techascent.muslim.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.composable.errorContent
import org.techascent.muslim.prayer.composable.loadingContent
import org.techascent.muslim.prayer.composable.successContent
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.state.PrayerTimeUiState

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun PrayerView(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    innerPadding: PaddingValues
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
            onNavigateToTasbeeh = onNavigateToTasbeeh,
            onNavigateToCompass = onNavigateToCompass,
            innerPadding = innerPadding
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun PrayerScreen(
    viewModel: PrayerTimeViewModel = koinViewModel<PrayerTimeViewModel>(),
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    PrayerContent(
        uiState = uiState,
        onNavigateToTasbeeh = onNavigateToTasbeeh,
        onNavigateToCompass = onNavigateToCompass,
        onHandleEvent = viewModel::onHandleEvent,
        innerPadding = innerPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerContent(
    uiState: PrayerTimeUiState,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onHandleEvent: (PrayerTimeEvent) -> Unit,
    innerPadding: PaddingValues
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            when (uiState) {
                is PrayerTimeUiState.Loading -> loadingContent()
                is PrayerTimeUiState.Success -> successContent(
                    uiModel = uiState.data,
                    onNavigateToTasbeeh = onNavigateToTasbeeh,
                    onNavigateToCompass = onNavigateToCompass,
                    onHandleEvent = onHandleEvent
                )

                is PrayerTimeUiState.Error -> errorContent()
            }
        }
    }

}

private fun handleEvent(event: PrayerTimeEvent, uriHandler: UriHandler) {
    when (event) {
        is PrayerTimeEvent.OpenExternalLink -> uriHandler.openUri(event.url)
    }
}