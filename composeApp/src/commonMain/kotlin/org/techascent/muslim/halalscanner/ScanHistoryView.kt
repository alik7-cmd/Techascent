package org.techascent.muslim.halalscanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_done
import apphub.composeapp.generated.resources.text_scan_history_clear
import apphub.composeapp.generated.resources.text_scan_history_clear_confirm
import apphub.composeapp.generated.resources.text_scan_history_empty
import apphub.composeapp.generated.resources.text_scan_history_empty_subtitle
import apphub.composeapp.generated.resources.title_scan_history
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.appbar.TrailingAction
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.composable.InformationContentV2
import org.techascent.muslim.halalscanner.state.ScanHistoryItem
import org.techascent.muslim.halalscanner.state.ScanSource
import org.techascent.muslim.halalscanner.state.toProductUiState
import org.techascent.shared.data.mapper.HalalStatus

private enum class HistoryPhase {
    LIST,
    DETAIL,
}

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun ScanHistoryView(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        ScanHistoryScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun ScanHistoryScreen(
    viewModel: HalalScannerViewModel,
    onNavigateBack: () -> Unit,
) {
    val historyItems by viewModel.historyState.collectAsState()
    var phase by remember { mutableStateOf(HistoryPhase.LIST) }
    var selectedItem by remember { mutableStateOf<ScanHistoryItem?>(null) }

    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "historyPhase",
    ) { currentPhase ->
        when (currentPhase) {
            HistoryPhase.LIST -> {
                HistoryListScreen(
                    items = historyItems,
                    onItemClick = { item ->
                        selectedItem = item
                        phase = HistoryPhase.DETAIL
                    },
                    onClearHistory = { viewModel.clearHistory() },
                    onNavigateBack = onNavigateBack,
                )
            }

            HistoryPhase.DETAIL -> {
                selectedItem?.let { item ->
                    HistoryDetailScreen(
                        item = item,
                        onNavigateBack = { phase = HistoryPhase.LIST },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryListScreen(
    items: List<ScanHistoryItem>,
    onItemClick: (ScanHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(Res.string.text_scan_history_clear)) },
            text = { Text(stringResource(Res.string.text_scan_history_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearDialog = false
                }) {
                    Text("Yes", color = Color(0xFFC62828))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("No")
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_scan_history),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
                action = if (items.isNotEmpty()) {
                    TrailingAction.TextButton(
                        text = stringResource(Res.string.text_scan_history_clear),
                        onClick = { showClearDialog = true },
                    )
                } else null,
            )
        },
    ) { innerPadding ->
        if (items.isEmpty()) {
            EmptyHistoryContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + ComposaSpacing.Small,
                    bottom = innerPadding.calculateBottomPadding() + ComposaSpacing.Large,
                    start = ComposaSpacing.Medium,
                    end = ComposaSpacing.Medium,
                ),
                verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            Text(
                text = "📋",
                fontSize = 48.sp,
            )
            Text(
                text = stringResource(Res.string.text_scan_history_empty),
                style = ComposaTheme.typography.bodyEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
            Text(
                text = stringResource(Res.string.text_scan_history_empty_subtitle),
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutralSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp),
            )
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: ScanHistoryItem,
    onClick: () -> Unit,
) {
    val statusColor = statusColorForHistory(item.halalStatus)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(ComposaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status indicator circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = sourceEmoji(item.source),
                fontSize = 22.sp,
            )
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Medium))

        Column(modifier = Modifier.weight(1f)) {
            // Brand or source label
            Text(
                text = item.brands ?: sourceLabel(item.source),
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Barcode or ingredients snippet
            val subtitle = when {
                !item.barcode.isNullOrBlank() -> "Barcode: ${item.barcode}"
                !item.ingredientsText.isNullOrBlank() -> item.ingredientsText.take(50) + if (item.ingredientsText.length > 50) "..." else ""
                else -> ""
            }
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = ComposaTheme.typography.caption,
                    color = ComposaTheme.color.textNeutralSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date/time
            Text(
                text = formatTimestamp(item.timestamp),
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Small))

        // Halal status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Text(
                    text = statusLabel(item.halalStatus),
                    style = ComposaTheme.typography.caption,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailScreen(
    item: ScanHistoryItem,
    onNavigateBack: () -> Unit,
) {
    val productUiState = remember(item) { item.toProductUiState() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = item.brands ?: "Product Details",
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ComposaTheme.color.backgroundAppBackground),
            contentAlignment = Alignment.TopCenter,
        ) {
            InformationContentV2(
                productUiState = productUiState,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────────

private fun formatTimestamp(epochMillis: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        "${localDateTime.year}-$month-$day  $hour:$minute"
    } catch (e: Exception) {
        ""
    }
}

private fun statusColorForHistory(status: HalalStatus): Color {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> Color(0xFF2E7D32)
        HalalStatus.HALAL_POSSIBLE -> Color(0xFF558B2F)
        HalalStatus.HALAL_DOUBTFUL -> Color(0xFFEF6C00)
        HalalStatus.NOT_HALAL -> Color(0xFFC62828)
        HalalStatus.UNKNOWN -> Color(0xFF757575)
    }
}

private fun statusLabel(status: HalalStatus): String {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> "Halal"
        HalalStatus.HALAL_POSSIBLE -> "Possible"
        HalalStatus.HALAL_DOUBTFUL -> "Doubtful"
        HalalStatus.NOT_HALAL -> "Not Halal"
        HalalStatus.UNKNOWN -> "Unknown"
    }
}

private fun sourceEmoji(source: ScanSource): String {
    return when (source) {
        ScanSource.SCANNER -> "📷"
        ScanSource.MANUAL_BARCODE -> "📦"
        ScanSource.MANUAL_INGREDIENTS -> "🧪"
    }
}

private fun sourceLabel(source: ScanSource): String {
    return when (source) {
        ScanSource.SCANNER -> "Scanned Product"
        ScanSource.MANUAL_BARCODE -> "Manual Barcode"
        ScanSource.MANUAL_INGREDIENTS -> "Ingredient Check"
    }
}

