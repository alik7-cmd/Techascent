package org.techascent.muslim.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun PrayerView(
    innerPadding: PaddingValues,
    onNavigateHalalScanner: () -> Unit,
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
            onStartTestAzan = viewModel::startRepeatingTestAzan,
            onStopTestAzan = viewModel::stopRepeatingTestAzan
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun PrayerScreen(
    viewModel: PrayerTimeViewModel = koinViewModel<PrayerTimeViewModel>(),
    onFetchPrayers: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onStartTestAzan: () -> Unit,
    onStopTestAzan: () -> Unit,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    PrayerContent(
        uiState = uiState,
        onFetchPrayers = onFetchPrayers,
        onNavigateHalalScanner = onNavigateHalalScanner,
        onUpdateNotification = viewModel::onUpdateNotification,
        innerPadding = innerPadding,
        onStartTestAzan = onStartTestAzan,
        onStopTestAzan = onStopTestAzan
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerContent(
    uiState: PrayerTimeUiState,
    onFetchPrayers: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
    onStartTestAzan: () -> Unit,
    onStopTestAzan: () -> Unit,
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
                is PrayerTimeUiState.Success -> {
                    successContent(
                        uiModel = uiState.data,
                        onNavigateHalalScanner = onNavigateHalalScanner,
                        onUpdateNotification = onUpdateNotification
                    )

                    // TODO: Remove test buttons before release
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ComposaSpacing.Medium),
                            horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small)
                        ) {
                            Button(
                                onClick = onStartTestAzan,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("▶ Start Test Azan\n(every 1 min × 5)")
                            }
                            Button(
                                onClick = onStopTestAzan,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF44336)
                                )
                            ) {
                                Text("⏹ Stop Test Azan")
                            }
                        }
                    }
                }

                is PrayerTimeUiState.Error -> errorContent(
                    onRetry = onFetchPrayers
                )
            }
        }
    }

}

private fun handleEvent(event: PrayerTimeEvent, uriHandler: UriHandler) {
    when (event) {
        is PrayerTimeEvent.OpenExternalLink -> uriHandler.openUri(event.url)
    }
}