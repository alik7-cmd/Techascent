package org.techascent.muslim.settings

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
import org.techascent.muslim.rememberAppLocale
import org.techascent.muslim.settings.event.SettingsEvent

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SettingsView(
    innerPadding: PaddingValues,
    onNavigateAbout: () -> Unit = {},
) {
    val viewModel = koinViewModel<SettingsViewModel>()

    // Detect the actual system locale and sync it to DataStore
    val currentAppLang = rememberAppLocale()
    LaunchedEffect(currentAppLang) {
        viewModel.syncLocaleFromSystem(currentAppLang.code)
    }

    ComposaTheme {
        val uriHandler = LocalUriHandler.current
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collect {
                handleEvent(
                    event = it,
                    uriHandler = uriHandler
                )
            }
        }

        SettingsScreen(
            viewModel = viewModel,
            innerPadding = innerPadding,
            onHandleEvent = viewModel::onHandleEvent,
            onNavigateAbout = onNavigateAbout,
        )
    }
}

@Composable
private fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onHandleEvent: (SettingsEvent) -> Unit,
    onNavigateAbout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val schoolPreference by viewModel.schoolPreference.collectAsState()
    val hapticPreference by viewModel.hapticPreference.collectAsState()
    val adhanPreference by viewModel.adhanPreference.collectAsState()
    val languagePreference by viewModel.languagePreference.collectAsState()
    SettingsScreenV2(
        uiState = uiState,
        schoolPreference = schoolPreference,
        hapticPreference = hapticPreference,
        adhanPreference = adhanPreference,
        languagePreference = languagePreference,
        innerPadding = innerPadding,
        onUpdateSchool = viewModel::updateSchoolPreference,
        onUpdateHaptic = viewModel::onUpdateHaptic,
        onUpdateAdhanNotification = viewModel::onUpdateAdhanNotification,
        onUpdateLanguage = viewModel::onUpdateLanguage,
        onHandleEvent = onHandleEvent,
        onNavigateAbout = onNavigateAbout,
    )

}

private fun handleEvent(event: SettingsEvent, uriHandler: UriHandler) {
    when (event) {
        is SettingsEvent.OpenExternalLink -> uriHandler.openUri(event.url)
    }
}