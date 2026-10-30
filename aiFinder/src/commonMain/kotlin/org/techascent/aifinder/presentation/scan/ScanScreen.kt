package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ScanScreen(state: ScanUiState, onAnalyze: () -> Unit, onBack: () -> Unit, onRetry: () -> Unit) {
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
            is ScanUiState.Success -> ScanContent(state.data, onAnalyze)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.ScanContent(
    data: ScanContent,
    onAnalyze: () -> Unit
) {
    items(listOf(0)) { _ ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle("Scan a space"); Text(
            "Take a clear photo of what you store.",
            color = AiFinderPalette.Muted
        )
            ScanCameraCard()
            InfoCard("Save into", data.selectedLocation)
            Text("Mockup only — image recognition is simulated.", color = AiFinderPalette.Muted)
            PrimaryAction("Analyze photo", onAnalyze)
        }
    }
}
