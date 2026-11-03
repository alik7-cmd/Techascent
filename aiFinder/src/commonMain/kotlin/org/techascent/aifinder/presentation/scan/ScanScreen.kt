package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.ic_google
import apphub.aifinder.generated.resources.scan_ai_notice
import apphub.aifinder.generated.resources.scan_location_required
import apphub.aifinder.generated.resources.location_save_into
import org.jetbrains.compose.resources.painterResource
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
    onChooseLocation: () -> Unit
) {
    val camera = remember { CameraCaptureController() }
    ScreenLayout(
        topBar = {
            Surface(color = AiFinderPalette.Background) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    BackButton(onBack)
                }
            }
        }
    ) {
        when (state) {
            is ScanUiState.Loading -> items(listOf(0)) { _ -> LoadingContent() }
            is ScanUiState.Error -> items(listOf(0)) { _ -> ErrorContent(state.message, onRetry) }
            is ScanUiState.Success -> scanContent(
                state.data,
                camera,
                selectedLocation,
                onChooseLocation
            ) { onAnalyze(camera) }
        }
    }
}

private fun LazyListScope.scanContent(
    data: ScanContent,
    camera: CameraCaptureController,
    selectedLocation: String,
    onChooseLocation: () -> Unit,
    onAnalyze: () -> Unit
) {
    items(listOf(0)) { _ ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle("Scan a space")
            Text(
                "Take a clear photo of what you store.",
                color = AiFinderPalette.Muted
            )
            ScanCameraCard(camera, isAnalyzing = data.isAnalyzing)
            data.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            InfoCard(
                title = stringResource(Res.string.location_save_into),
                subtitle = selectedLocation.ifBlank { stringResource(Res.string.scan_location_required) },
                subtitleColor = if (selectedLocation.isBlank()) MaterialTheme.colorScheme.error else AiFinderPalette.Muted,
                onClick = onChooseLocation,
                trailingContent = {
                    IconButton(onClick = onChooseLocation) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            //Text(stringResource(Res.string.scan_ai_notice), color = AiFinderPalette.Muted)
            PrimaryAction(
                label = if (data.isAnalyzing) "Analyzing…" else "Analyze photo",
                onClick = { if (selectedLocation.isNotBlank() && !data.isAnalyzing) onAnalyze() },
                isLoading = data.isAnalyzing,
                enabled = selectedLocation.isNotBlank() && !data.isAnalyzing
            )
        }
    }
}
