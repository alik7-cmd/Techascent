package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.aifinder.presentation.components.ErrorContent
import org.techascent.aifinder.presentation.components.LoadingContent
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
    onSearch: () -> Unit,
    onScan: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showSpaces: () -> Unit = {
        if (uiState is HomeUiState.Success) {
            scope.launch { listState.animateScrollToItem(3) }
        }
    }

    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> item { LoadingContent() }
                is HomeUiState.Error -> item {
                    ErrorContent(
                        (uiState as HomeUiState.Error).message,
                        viewModel::retry
                    )
                }

                is HomeUiState.Success -> {
                    item(key = "header") { HomeHeader() }
                    item(key = "search") { HomeSearch(onSearch) }
                    item(key = "scan") { ScanPrompt(onScan) }
                    item(key = "spaces") { SpacesHeader(showSpaces) }
                    items(
                        (uiState as HomeUiState.Success).data.spaces,
                        key = { it.id }) { location ->
                        SpaceCard(location, onSearch)
                    }
                }
            }
        }
    }
}
