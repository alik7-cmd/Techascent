package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import org.koin.compose.viewmodel.koinViewModel
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.home_spaces_error_title
import apphub.aifinder.generated.resources.inventory_sync_error
import apphub.aifinder.generated.resources.inventory_sync_error_title
import apphub.aifinder.generated.resources.error_card_retry
import apphub.aifinder.generated.resources.login_error_dismiss
import apphub.aifinder.generated.resources.inventory_syncing
import apphub.aifinder.generated.resources.inventory_refresh
import apphub.aifinder.generated.resources.qr_scan_action
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.ErrorCard
import org.techascent.aifinder.presentation.components.EmptyListCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.presentation.scan.ScanContent
import org.techascent.aifinder.config.AppConfig
import org.techascent.aifinder.config.HomeLayout

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
    onDismissSyncError: () -> Unit,
    onScanLabel: () -> Unit,
    onItem: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboard = AppConfig.HOME_LAYOUT == HomeLayout.DASHBOARD

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = AiFinderPalette.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header") {
                HomeHeader(onAccount, scanBalance, onRetryBalance, onGetScans)
            }
            item(key = "search") { HomeSearch(onSearch) }
            if (dashboard) {
                when (val state = uiState) {
                    is HomeUiState.Success -> item(key = "overview") { HomeInventoryOverview(state.data) }
                    is HomeUiState.Loading -> item(key = "overview-loading") { HomeOverviewSkeleton() }
                    is HomeUiState.Error -> Unit
                }
                item(key = "quick-actions") {
                    HomeQuickActions(onScan, isCheckingScan, onAddLocation, onSeeAll)
                }
            } else {
                item(key = "scan") { ScanPrompt(onScan, isCheckingScan, compact = true) }
            }
            item(key = "sync") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (syncState.failed) {
                        InventoryRefreshError(onDismissSyncError)
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
                                stringResource(when {
                                    syncState.busy -> Res.string.inventory_syncing
                                    syncState.failed -> Res.string.error_card_retry
                                    else -> Res.string.inventory_refresh
                                }),
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
                    if (dashboard && state.data.recentItems.isNotEmpty()) {
                        item(key = "recent-heading") { HomeRecentHeading() }
                        items(state.data.recentItems, key = { "recent-${it.item.id}" }) { entry ->
                            HomeRecentItem(entry, onClick = { onItem(entry.item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryRefreshError(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(20.dp),
        color = AiFinderPalette.ErrorContainer,
        border = BorderStroke(1.dp, AiFinderPalette.ErrorBorder),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(36.dp).background(AiFinderPalette.Error.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = AiFinderPalette.Error, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.inventory_sync_error_title),
                    color = AiFinderPalette.ErrorText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(Res.string.inventory_sync_error),
                    color = AiFinderPalette.ErrorText.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.login_error_dismiss),
                    tint = AiFinderPalette.ErrorText.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
