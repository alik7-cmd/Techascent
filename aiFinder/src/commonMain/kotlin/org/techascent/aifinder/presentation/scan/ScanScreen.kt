package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ScanScreen(
    state: ScanUiState,
    onAnalyze: (CameraCaptureController) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    selectedLocation: String,
    onChooseLocation: () -> Unit,
    onRetryBalance: () -> Unit,
    onGetScans: () -> Unit,
) {
    val camera = remember { CameraCaptureController() }
    ScreenLayout(
        topBar = {
            ScreenTopBar(stringResource(Res.string.home_scan_action), onBack)
        }
    ) {
        when (state) {
            is ScanUiState.Loading -> item { LoadingContent() }
            is ScanUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is ScanUiState.Success -> scanContent(
                state.data,
                camera,
                selectedLocation,
                onChooseLocation,
                onRetryBalance,
                onGetScans,
            ) { onAnalyze(camera) }
        }
    }
}

private fun LazyListScope.scanContent(
    data: ScanContent,
    camera: CameraCaptureController,
    selectedLocation: String,
    onChooseLocation: () -> Unit,
    onRetryBalance: () -> Unit,
    onGetScans: () -> Unit,
    onAnalyze: () -> Unit
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(Res.string.scan_description),
                color = AiFinderPalette.Muted
            )
            //ScanBalanceCard(data, onRetryBalance, onGetScans)
            ScanCameraCard(camera, isAnalyzing = data.isAnalyzing)
            data.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            InfoCard(
                title = stringResource(Res.string.location_save_into),
                subtitle = selectedLocation.ifBlank { stringResource(Res.string.scan_location_required) },
                subtitleColor = if (selectedLocation.isBlank()) MaterialTheme.colorScheme.error else AiFinderPalette.Muted,
                containerColor = if (selectedLocation.isBlank()) AiFinderPalette.Mint else AiFinderPalette.Surface,
                onClick = if (data.isAnalyzing) null else onChooseLocation,
                leadingContent = {
                    Icon(Icons.Outlined.MyLocation, null, tint = AiFinderPalette.Teal)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AiFinderPalette.Teal)
                }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Icon(
                    Icons.Outlined.Info, null, tint = AiFinderPalette.Teal,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    stringResource(Res.string.scan_credit_notice),
                    color = AiFinderPalette.Ink,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            }
            //Text(stringResource(Res.string.scan_ai_notice), color = AiFinderPalette.Muted)
            PrimaryAction(
                label = stringResource(if (data.isAnalyzing) Res.string.scan_analyzing else Res.string.scan_analyze),
                onClick = { if (selectedLocation.isNotBlank() && !data.isAnalyzing) onAnalyze() },
                isLoading = data.isAnalyzing,
                enabled = selectedLocation.isNotBlank() && !data.isAnalyzing && !data.requiresPurchase
            )
        }
    }
}
