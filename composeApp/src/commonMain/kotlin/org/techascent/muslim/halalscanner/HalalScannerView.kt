package org.techascent.muslim.halalscanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_product_not_found
import apphub.composeapp.generated.resources.text_scan_again
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerColors
import org.ncgroup.kscan.ScannerView
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.appbar.TrailingAction
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.composable.InformationContentV2
import org.techascent.muslim.halalscanner.composable.LoadingContent
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.prayer.composable.ErrorCard

/**
 * Represents the visual phase of the scanner screen.
 */
private enum class ScannerPhase {
    SCANNING,
    RESULT,
}

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
    var phase by remember { mutableStateOf(ScannerPhase.SCANNING) }

    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "scannerPhase",
    ) { currentPhase ->
        when (currentPhase) {
            ScannerPhase.SCANNING -> {
                // ── Camera / scanner – full-bleed, no Scaffold chrome ────────
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            zoomControllerContentColor = ComposaTheme.color.textNeutral,
                        ),
                    ) { result ->
                        when (result) {
                            is BarcodeResult.OnSuccess -> {
                                phase = ScannerPhase.RESULT
                                onFetchProduct(result.barcode.data)
                            }

                            is BarcodeResult.OnFailed -> {
                                phase = ScannerPhase.RESULT
                            }

                            is BarcodeResult.OnCanceled -> {
                                onNavigateBack()
                            }
                        }
                    }
                }
            }

            ScannerPhase.RESULT -> {
                // ── Result screen – proper Scaffold with compact app-bar ─────
                ResultScreen(
                    uiState = uiState,
                    onNavigateBack = onNavigateBack,
                )
            }
        }
    }
}

// ─── Result screen with its own Scaffold + TopAppBar ────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(
    uiState: HalalScannerUiState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = "Scan Result",
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
                action = when (uiState) {
                    is HalalScannerUiState.Error,
                    is HalalScannerUiState.Success -> TrailingAction.TextButton(
                        text = stringResource(Res.string.text_scan_again),
                        onClick = onNavigateBack,
                    )
                    else -> null
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ComposaTheme.color.backgroundAppBackground),
            contentAlignment = when (uiState) {
                is HalalScannerUiState.Success -> Alignment.TopCenter
                else -> Alignment.Center
            },
        ) {
            when (uiState) {
                is HalalScannerUiState.Loading -> {
                    LoadingContent(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is HalalScannerUiState.Error -> {
                    ErrorCard(
                        description = stringResource(Res.string.text_product_not_found),
                        buttonText = stringResource(Res.string.text_cancel),
                        onRetry = onNavigateBack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is HalalScannerUiState.Success -> {
                    InformationContentV2(
                        productUiState = uiState.data,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is HalalScannerUiState.Init -> Unit
            }
        }
    }
}
