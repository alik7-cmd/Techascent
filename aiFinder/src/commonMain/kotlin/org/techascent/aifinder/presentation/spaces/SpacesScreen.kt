package org.techascent.aifinder.presentation.spaces

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
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
    onAddPlace: () -> Unit,
) {
    ScreenLayout(topBar = { ScreenTopBar(stringResource(Res.string.spaces_title), onBack) }) {
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
                item(key = "add-place") {
                    androidx.compose.material3.TextButton(onClick = onAddPlace) {
                        androidx.compose.material3.Text(
                            stringResource(Res.string.home_add_location), color = AiFinderPalette.Teal)
                    }
                }
                if (state.spaces.isEmpty()) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16)) {
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
                        parentPath = entry.parentPath,
                        coverPhotoId = entry.coverPhotoId,
                    )
                }
            }
        }
    }
}
