package org.techascent.muslim.halalscanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.composable.InformationContent
import org.techascent.muslim.halalscanner.composable.LoadingContent
import org.techascent.muslim.halalscanner.state.HalalScannerUiState

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun HalalScannerView(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        HalalScannerScreen(
            viewModel = viewModel,
            onFetchProduct = viewModel::fetchProductByBarcode,
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun HalalScannerScreen(
    viewModel: HalalScannerViewModel = koinViewModel<HalalScannerViewModel>(),
    onFetchProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    // Pass down the entire uiState and the padding
    HalalScannerContent(
        uiState = uiState,
        onFetchProduct = onFetchProduct,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HalalScannerContent(
    uiState: HalalScannerUiState,
    onFetchProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // The main Scaffold now applies the padding from the parent
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) { paddingValues -> // This is the padding from this Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Use the new padding
            contentAlignment = Alignment.Center
        ) {
            var shouldShowScanner by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = shouldShowScanner,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                ScannerView(
                    modifier = Modifier.fillMaxSize(),
                    codeTypes = listOf(
                        BarcodeFormats.FORMAT_QR_CODE,
                        BarcodeFormats.FORMAT_EAN_13,
                    ),
                    showUi = true
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
                            shouldShowScanner = false
                            onFetchProduct(result.barcode.data)
                        }

                        is BarcodeResult.OnFailed -> {
                            shouldShowScanner = false
                            println("Error: ${result.exception.message}")
                        }

                        is BarcodeResult.OnCanceled -> {
                            shouldShowScanner = false
                            onNavigateBack()
                            println("OnCanceled: canceled by user")
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !shouldShowScanner,
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is HalalScannerUiState.Error, HalalScannerUiState.Init -> Unit
                    is HalalScannerUiState.Loading -> LoadingContent()
                    is HalalScannerUiState.Success -> {
                        InformationContent(
                            productUiState = uiState.data,
                            onNavigateBack = onNavigateBack
                        )
                    }
                }
            }

        }
    }
}


