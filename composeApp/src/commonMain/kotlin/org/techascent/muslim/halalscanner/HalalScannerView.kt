package org.techascent.muslim.halalscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.reset_warning_cancel_text
import apphub.composeapp.generated.resources.text_okay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.showNativeResetDialog

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun HalalScannerView(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        // Pass the innerPadding from the parent Scaffold
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
                        onFetchProduct(result.barcode.data)
                    }

                    is BarcodeResult.OnFailed -> {
                        println("Error: ${result.exception.message}")
                    }

                    is BarcodeResult.OnCanceled -> {
                    }
                }
            }

            when (uiState) {
                is HalalScannerUiState.Error -> {

                }

                is HalalScannerUiState.Init -> {

                }

                is HalalScannerUiState.Loading -> CircularProgressIndicator()
                is HalalScannerUiState.Success -> {
                    val halalResult = uiState.data.halalResult
                    showNativeResetDialog(
                        title = halalResult.status.name,
                        message = halalResult.reason,
                        confirmText = stringResource(Res.string.text_okay),
                        cancelText = "",
                        onConfirm = onNavigateBack,
                        onCancel = {}
                    )
                }
            }
        }
    }
}
