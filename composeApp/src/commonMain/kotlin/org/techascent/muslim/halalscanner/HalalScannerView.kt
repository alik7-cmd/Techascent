package org.techascent.muslim.halalscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.state.HalalScannerUiState


@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun HalalScannerView(
    innerPadding: PaddingValues
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        HalalScannerScreen(
            viewModel = viewModel,
            innerPadding = innerPadding
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun HalalScannerScreen(
    viewModel: HalalScannerViewModel = koinViewModel<HalalScannerViewModel>(),
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()

}

@Composable
private fun HalalScannerContent(
    uiState: HalalScannerUiState
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) {


    }
}
