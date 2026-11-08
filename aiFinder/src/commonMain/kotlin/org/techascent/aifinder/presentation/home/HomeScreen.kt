package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.home_spaces_error_title
import apphub.aifinder.generated.resources.subscription_access_error
import apphub.aifinder.generated.resources.inventory_sync_error
import apphub.aifinder.generated.resources.inventory_syncing
import apphub.aifinder.generated.resources.inventory_refresh
import apphub.aifinder.generated.resources.qr_scan_action
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.ErrorCard
import org.techascent.aifinder.presentation.components.EmptyListCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.presentation.scan.ScanBalanceCardV3
import org.techascent.aifinder.presentation.scan.ScanContent
import org.techascent.aifinder.presentation.scan.CreditCardComparison
import org.techascent.aifinder.config.CreditCardPreviewConfig

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
    onSearch: () -> Unit,
    onScan: () -> Unit,
    isCheckingScan: Boolean = false,
    onAccount: () -> Unit,
    onSpace: (String) -> Unit,
    onSeeAll: () -> Unit,
    scanBalance: ScanContent,
    onRetryBalance: () -> Unit,
    onGetScans: () -> Unit,
    onAddLocation: () -> Unit,
    syncState: org.techascent.aifinder.presentation.inventory.InventorySyncState,
    onSync: () -> Unit,
    onScanLabel: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = AiFinderPalette.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header") { HomeHeader(onAccount) }
            item(key = "search") { HomeSearch(onSearch) }
            item(key = "scan") { ScanPrompt(onScan, isCheckingScan) }
            item(key = "sync") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (syncState.failed) {
                        androidx.compose.material3.Text(
                            stringResource(apphub.aifinder.generated.resources.Res.string.inventory_sync_error),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val chipColors = AssistChipDefaults.assistChipColors(
                            labelColor = AiFinderPalette.Teal,
                            leadingIconContentColor = AiFinderPalette.Teal,
                            disabledLabelColor = AiFinderPalette.Teal.copy(alpha = 0.65f),
                        )
                        AssistChip(
                            onClick = onSync,
                            enabled = !syncState.busy,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, AiFinderPalette.Teal.copy(alpha = if (syncState.busy) 0.25f else 0.5f)),
                            colors = chipColors,
                            leadingIcon = { Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(18.dp)) },
                            label = { Text(
                                stringResource(if (syncState.busy) Res.string.inventory_syncing
                                    else Res.string.inventory_refresh),
                            ) },
                        )
                        AssistChip(
                            onClick = onScanLabel,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, AiFinderPalette.Teal.copy(alpha = 0.5f)),
                            colors = chipColors,
                            leadingIcon = {
                                Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                            },
                            label = { Text(stringResource(Res.string.qr_scan_action)) },
                        )
                    }
                }
            }
            item(key = "credits") { ScanBalanceCardV3(scanBalance, onRetryBalance, onGetScans) }
            if (CreditCardPreviewConfig.SHOW_COMPARISON) {
                item(key = "credit-design-comparison") { CreditCardComparison() }
            }
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    item(key = "spaces-skeleton") { SpacesHeaderSkeleton() }
                    items(
                        HOME_SKELETON_CARDS,
                        key = { "space-skeleton-$it" }) { SpaceCardSkeleton() }
                }

                is HomeUiState.Error -> item(key = "error") {
                    ErrorCard(
                        title = stringResource(Res.string.home_spaces_error_title),
                        message = state.message,
                        onRetry = viewModel::retry,
                    )
                }

                is HomeUiState.Success -> {
                    item(key = "spaces") {
                        SpacesHeader(
                            onSeeAll = if (state.data.spaces.isEmpty()) onAddLocation else onSeeAll,
                            isEmpty = state.data.spaces.isEmpty(),
                        )
                    }
                    if (state.data.spaces.isEmpty()) {
                        item(key = "empty") {
                            EmptyListCard()
                        }
                    }
                    items(state.data.spaces, key = { it.location.id }) { entry ->
                        SpaceCard(
                            entry.location,
                            onClick = { onSpace(entry.location.id) },
                            parentPath = entry.parentPath,
                            coverPhotoId = entry.coverPhotoId,
                        )
                    }
                }
            }
        }
    }
}
