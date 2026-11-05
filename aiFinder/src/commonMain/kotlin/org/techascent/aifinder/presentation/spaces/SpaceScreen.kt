package org.techascent.aifinder.presentation.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun SpaceScreen(
    state: SpaceUiState,
    onItem: (String) -> Unit,
    onScan: () -> Unit,
    isCheckingScan: Boolean = false,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    ScreenLayout(topBar = { BackBar(onBack) }) {
        when (state) {
            is SpaceUiState.Loading -> item { LoadingContent() }
            is SpaceUiState.Error -> item {
                ErrorCard(
                    title = stringResource(Res.string.home_spaces_error_title),
                    message = state.message,
                    onRetry = onRetry,
                )
            }

            is SpaceUiState.Success -> {
                val location = state.data.location
                if (location == null) {
                    item {
                        Text(
                            stringResource(Res.string.space_not_found),
                            color = AiFinderPalette.Muted
                        )
                    }
                    return@ScreenLayout
                }
                item(key = "title") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionTitle(location.name)
                        if (state.data.path != location.name) {
                            Text(state.data.path, color = AiFinderPalette.Muted, fontSize = 14.sp)
                        }
                        Text(
                            stringResource(
                                Res.string.home_space_summary,
                                location.spaceCount,
                                location.itemCount
                            ),
                            color = AiFinderPalette.Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                if (state.data.items.isEmpty()) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EmptyListCard(
                                titleRes = Res.string.space_empty_title,
                                descriptionRes = Res.string.space_empty_message,
                            )
                            PrimaryAction(
                                stringResource(Res.string.home_scan_action),
                                onScan,
                                isLoading = isCheckingScan
                            )
                        }
                    }
                }
                items(state.data.items, key = { it.item.id }) { located ->
                    SpaceItemRow(
                        located,
                        spacePath = state.data.path,
                        onClick = { onItem(located.item.id) })
                }
            }
        }
    }
}

@Composable
private fun SpaceItemRow(located: LocatedItem, spacePath: String, onClick: () -> Unit) {
    // Items in nested locations show where they are relative to this space.
    val subPath =
        located.path.removePrefix(spacePath).removePrefix(" › ").takeIf { it.isNotEmpty() }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AiFinderPalette.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ItemPhoto(
            photoId = located.item.photoId,
            contentDescription = null,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Outlined.Category, contentDescription = null, tint = AiFinderPalette.Teal)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                located.item.name,
                color = AiFinderPalette.Ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                listOfNotNull(located.item.category, subPath).joinToString(" • "),
                color = AiFinderPalette.Muted,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
