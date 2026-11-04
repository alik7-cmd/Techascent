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
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.aifinder.presentation.components.ErrorContent
import org.techascent.aifinder.presentation.components.InfoCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette

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
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header") { HomeHeader(onAccount) }
            item(key = "search") { HomeSearch(onSearch) }
            item(key = "scan") { ScanPrompt(onScan, isCheckingScan) }
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    item(key = "spaces-skeleton") { SpacesHeaderSkeleton() }
                    items(HOME_SKELETON_CARDS, key = { "space-skeleton-$it" }) { SpaceCardSkeleton() }
                }
                is HomeUiState.Error -> item(key = "error") { ErrorContent(state.message, viewModel::retry) }
                is HomeUiState.Success -> {
                    item(key = "spaces") { SpacesHeader(onSeeAll, showSeeAll = state.data.spaces.isNotEmpty()) }
                    if (state.data.spaces.isEmpty()) {
                        item(key = "empty") {
                            InfoCard(
                                title = stringResource(Res.string.home_spaces_empty_title),
                                subtitle = stringResource(Res.string.home_spaces_empty_message),
                                onClick = {}//onScan.takeUnless { isCheckingScan }
                            )
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
