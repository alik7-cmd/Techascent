package org.techascent.muslim.halalscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.icerock.moko.permissions.PermissionState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.state.HalalScannerUiState

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun HalalScannerView(
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        // Pass the innerPadding from the parent Scaffold
        HalalScannerScreen(
            viewModel = viewModel,
            onFetchProduct = viewModel::fetchProductByBarcode,
            onUpdateScannerVisibility = viewModel::updateScannerVisibility
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun HalalScannerScreen(
    viewModel: HalalScannerViewModel = koinViewModel<HalalScannerViewModel>(),
    onFetchProduct: (String) -> Unit,
    onUpdateScannerVisibility: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    // Pass down the entire uiState and the padding
    HalalScannerContent(
        uiState = uiState,
        onFetchProduct = onFetchProduct,
    )
}

@Composable
private fun HalalScannerContent(
    uiState: HalalScannerUiState,
    onFetchProduct: (String) -> Unit,
    onUpdateScannerVisibility: (Boolean) -> Unit = {},
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
            if (uiState.shouldShowScanner) {
                ScannerView(
                    modifier = Modifier.fillMaxSize(),
                    codeTypes = listOf(
                        BarcodeFormats.FORMAT_QR_CODE,
                        BarcodeFormats.FORMAT_EAN_13,
                    ),
                    showUi = true
                ) { result ->
                    // Only trigger a new fetch if not currently loading
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
                            onUpdateScannerVisibility(false)
                            onFetchProduct(result.barcode.data)
                        }

                        is BarcodeResult.OnFailed -> {
                            println("Error: ${result.exception.message}")
                            onUpdateScannerVisibility(false)
                        }

                        is BarcodeResult.OnCanceled -> {
                            onUpdateScannerVisibility(false)
                        }
                    }
                }
            }
            // Scanner is always visible in the background
            // UI Overlay based on the state
            when {
                // When loading is true
                uiState.loading -> {
                    CircularProgressIndicator()
                }


                // When we have a result (success or error)
                uiState.halalResult != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ComposaTheme.color.backgroundAppBackground.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = uiState.halalResult.reason,
                            style = ComposaTheme.typography.titleLarge,
                            color = ComposaTheme.color.textNeutral,
                            textAlign = TextAlign.Center
                        )
                        // You could add a button here to scan again
                    }
                }
            }
        }
    }
}
