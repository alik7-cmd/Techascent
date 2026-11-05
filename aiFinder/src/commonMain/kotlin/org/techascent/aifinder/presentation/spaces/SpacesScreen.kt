package org.techascent.aifinder.presentation.spaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.home.HOME_SKELETON_CARDS
import org.techascent.aifinder.presentation.home.SpaceCard
import org.techascent.aifinder.presentation.home.SpaceCardSkeleton
import org.techascent.aifinder.presentation.home.SpacesHeaderSkeleton
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun SpacesScreen(
    state: SpacesUiState,
    onSpace: (String) -> Unit,
    onScan: () -> Unit,
    isCheckingScan: Boolean = false,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    ScreenLayout(topBar = { BackBar(onBack) }) {
        item(key = "title") { SectionTitle(stringResource(Res.string.spaces_title)) }
        when (state) {
            is SpacesUiState.Loading -> {
                item(key = "spaces-skeleton") { SpacesHeaderSkeleton() }
                items(HOME_SKELETON_CARDS, key = { "space-skeleton-$it" }) { SpaceCardSkeleton() }
            }

            is SpacesUiState.Error -> item {
                ErrorCard(
                    title = stringResource(Res.string.home_spaces_error_title),
                    message = state.message,
                    onRetry = onRetry,
                )
            }

            is SpacesUiState.Success -> {
                if (state.spaces.isEmpty()) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EmptyListCard()
                            PrimaryAction(
                                stringResource(Res.string.home_scan_action),
                                onScan,
                                isLoading = isCheckingScan,
                            )
                        }
                    }
                }
                items(state.spaces, key = { it.location.id }) { entry ->
                    SpaceCard(
                        entry.location,
                        onClick = { onSpace(entry.location.id) },
                        parentPath = entry.parentPath
                    )
                }
            }
        }
    }
}

@Composable
internal fun BackBar(onBack: () -> Unit) {
    Surface(color = AiFinderPalette.Background) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            BackButton(onBack)
        }
    }
}
