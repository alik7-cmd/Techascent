package org.techascent.aifinder.presentation.scan

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.config.AnalysisConfig
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ScanCaptureScreen(
    state: ScanUiState,
    selectedLocation: String,
    onCapture: (CameraCaptureController) -> Unit,
    onAnalyze: (CameraCaptureController) -> Unit,
    onRetake: () -> Unit,
    onChooseLocation: () -> Unit,
    onBack: () -> Unit,
) {
    val camera = remember { CameraCaptureController() }
    val data = (state as? ScanUiState.Success)?.data ?: ScanContent()
    Scaffold(
        containerColor = AiFinderPalette.Background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ScreenTopBar(stringResource(
            if (data.previewPhoto == null) Res.string.scan_take_photo else Res.string.scan_photo_preview), onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(selectedLocation.ifBlank { stringResource(Res.string.scan_place_first) },
                color = AiFinderPalette.Teal, maxLines = 2)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                val photo = data.previewPhoto
                if (photo == null) ScanCameraCard(camera, false, Modifier.fillMaxSize(), expanded = true,
                    framingHint = stringResource(Res.string.scan_frame_hint))
                else ItemPhoto(photo.id, stringResource(Res.string.scan_photo_preview), RoundedCornerShape(24.dp),
                    Modifier.fillMaxSize(), jpegBytes = photo.jpegBytes, contentScale = ContentScale.Fit)
            }
            if (data.isAnalyzing) {
                AiAnalysisFeedback()
            } else {
                data.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (selectedLocation.isBlank()) {
                    OutlinedButton(onClick = onChooseLocation, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.scan_choose_continue))
                    }
                }
                if (data.previewPhoto != null) {
                    Text(stringResource(Res.string.scan_preview_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = onRetake, enabled = !data.isCapturing,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Text(stringResource(Res.string.scan_retake), color = AiFinderPalette.Teal)
                    }
                }
                PrimaryAction(
                    stringResource(if (data.previewPhoto == null) Res.string.scan_take_photo else Res.string.scan_analyze),
                    { if (data.previewPhoto == null) onCapture(camera) else onAnalyze(camera) },
                    isLoading = data.isCapturing,
                    enabled = selectedLocation.isNotBlank() && !data.requiresPurchase && !data.isCapturing,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AiAnalysisFeedback() {
    val transition = rememberInfiniteTransition(label = "ai-analysis")
    val phase by transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "scan-sweep")
    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AiFinderPalette.Teal.copy(alpha = 0.25f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(AiFinderPalette.Teal.copy(alpha = 0.08f + phase * 0.12f))
                        drawCircle(AiFinderPalette.Teal.copy(alpha = 0.4f), style = Stroke(1.dp.toPx()))
                        drawLine(AiFinderPalette.Teal.copy(alpha = 0.5f),
                            Offset(size.width * 0.2f, size.height * (0.25f + phase * 0.5f)),
                            Offset(size.width * 0.8f, size.height * (0.25f + phase * 0.5f)), 2.dp.toPx())
                    }
                    Icon(Icons.Outlined.AutoAwesome, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(if (AnalysisConfig.USE_FAKE_ANALYSIS)
                        Res.string.scan_preparing_samples else Res.string.scan_finding_feedback),
                        color = AiFinderPalette.Ink, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(Res.string.scan_feedback_hint), color = AiFinderPalette.Muted,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            ShimmerBlock(Modifier.fillMaxWidth(0.85f).height(8.dp))
            ShimmerBlock(Modifier.fillMaxWidth(0.6f).height(8.dp))
        }
    }
}
