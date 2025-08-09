package org.techascent.muslim.settings

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.settings.composable.appearanceContent
import org.techascent.muslim.settings.composable.ratingContent
import org.techascent.muslim.settings.state.SettingsUiState

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SettingsView(
    innerPadding: PaddingValues
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    ComposaTheme {
        SettingsScreen(
            viewModel = viewModel,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        innerPadding = innerPadding
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
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
            appearanceContent(appearanceUiModel = uiState.appearanceUiModel)
            ratingContent(aboutUsUiModel = uiState.aboutUsUiModel)
        }
    }

}