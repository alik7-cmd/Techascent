package org.techascent.muslim.halalscanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
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
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_product_not_found
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerColors
import org.ncgroup.kscan.ScannerView
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.composable.InformationContent
import org.techascent.muslim.halalscanner.composable.LoadingContent
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.prayer.composable.ErrorCard

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
    HalalScannerContent(
        uiState = uiState,
        onFetchProduct = onFetchProduct,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HalalScannerContent(
    uiState: HalalScannerUiState,
    onFetchProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var shouldShowScanner by remember { mutableStateOf(true) }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) { paddingValues -> // This is the padding from this Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = ComposaTheme.color.backgroundAppBackground),
            contentAlignment = Alignment.Center
        ) {

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
                    colors = ScannerColors(
                        headerContainerColor = ComposaTheme.color.backgroundAppBackground,
                        zoomControllerContainerColor = ComposaTheme.color.backgroundAppBackground,
                        barcodeFrameColor = ComposaTheme.color.backgroundAppBackground,
                        headerTitleColor = ComposaTheme.color.textNeutral,
                        headerNavigationIconColor = ComposaTheme.color.textNeutral,
                        headerActionIconColor = ComposaTheme.color.textNeutral,
                        zoomControllerContentColor = ComposaTheme.color.textNeutral
                    )
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            shouldShowScanner = false
                            onFetchProduct(result.barcode.data)
                        }

                        is BarcodeResult.OnFailed -> {
                            shouldShowScanner = false
                        }

                        is BarcodeResult.OnCanceled -> {
                            shouldShowScanner = false
                            onNavigateBack()
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !shouldShowScanner,
                modifier = Modifier.fillMaxSize()
            ) {
                val contentModifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(vertical = ComposaSpacing.Medium)

                when (uiState) {
                    is HalalScannerUiState.Error -> ErrorCard(
                        description = stringResource(Res.string.text_product_not_found),
                        buttonText = stringResource(Res.string.text_cancel),
                        onRetry = onNavigateBack,
                        modifier = contentModifier
                    )

                    is HalalScannerUiState.Loading -> LoadingContent(
                        modifier = contentModifier
                    )

                    is HalalScannerUiState.Success -> {
                        InformationContent(
                            modifier = contentModifier,
                            productUiState = uiState.data,
                            onNavigateBack = onNavigateBack
                        )
                    }

                    is HalalScannerUiState.Init -> Unit
                }
            }

        }
    }
}


