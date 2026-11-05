package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.home_spaces_error_title
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.ErrorCard
import org.techascent.aifinder.presentation.components.EmptyListCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.presentation.scan.ScanBalanceCard
import org.techascent.aifinder.presentation.scan.ScanContent

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
            item(key = "credits") { ScanBalanceCard(scanBalance, onRetryBalance, onGetScans) }
            item(key = "search") { HomeSearch(onSearch) }
            item(key = "scan") { ScanPrompt(onScan, isCheckingScan) }
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
                            onSeeAll,
                            showSeeAll = state.data.spaces.isNotEmpty()
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
                        )
                    }
                }
            }
        }
    }
}
