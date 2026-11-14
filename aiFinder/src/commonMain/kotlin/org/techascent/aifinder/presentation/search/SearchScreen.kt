package org.techascent.aifinder.presentation.search

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.composa.theming.ComposaTheme

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onItemSelected: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onConfirm: (org.techascent.aifinder.domain.model.LocatedItem) -> Unit,
    onMove: (org.techascent.aifinder.domain.model.LocatedItem) -> Unit,
    busy: Boolean,
    actionError: String?,
) {
    ScreenLayout(
        topBar = {
            ScreenTopBar(stringResource(Res.string.search_title), onBack)
        }
    ) {
        when (state) {
            is SearchUiState.Loading -> searchLoadingContent()
            is SearchUiState.Error -> item {
                ErrorCard(
                    title = stringResource(Res.string.home_spaces_error_title),
                    message = state.message,
                    onRetry = onRetry,
                )
            }
            is SearchUiState.Success -> searchContent(state.data, onQueryChange, onItemSelected,
                onConfirm, onMove, busy, actionError)
        }
    }
}

private fun LazyListScope.searchContent(
    data: SearchContent,
    onQueryChange: (String) -> Unit,
    onItemSelected: (String) -> Unit,
    onConfirm: (org.techascent.aifinder.domain.model.LocatedItem) -> Unit,
    onMove: (org.techascent.aifinder.domain.model.LocatedItem) -> Unit,
    busy: Boolean,
    actionError: String?,
) {
    item(key = "query") {
        val focusManager = LocalFocusManager.current
        TextField(
            value = data.query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(Res.string.home_search_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AiFinderPalette.Surface,
                unfocusedContainerColor = AiFinderPalette.Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = AiFinderPalette.Ink,
                unfocusedTextColor = AiFinderPalette.Ink,
                focusedLeadingIconColor = AiFinderPalette.Ink,
                unfocusedLeadingIconColor = AiFinderPalette.Ink,
                cursorColor = AiFinderPalette.Teal
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )
        Text(
            stringResource(Res.string.search_tip),
            modifier = Modifier.padding(horizontal = AiFinderSpacing.dp12, vertical = AiFinderSpacing.dp5),
            style = ComposaTheme.typography.caption,
            color = AiFinderPalette.Muted,
            fontSize = 14.sp
        )
    }
    if (data.results.isNotEmpty()) item(key = "count") {
        Text(
            stringResource(
                if (data.results.size == 1) Res.string.search_one_found else Res.string.search_items_found,
                data.results.size
            ),
            color = AiFinderPalette.Muted,
            modifier = Modifier.padding(vertical = AiFinderSpacing.dp8)
        )
    }
    if (data.results.isEmpty()) {
        item(key = "empty") {
            EmptyListCard(
                titleRes = if (data.hasItems) Res.string.search_empty_matches_title else Res.string.search_empty_inventory_title,
                description = if (data.hasItems) {
                    stringResource(Res.string.search_empty_matches_description, data.query)
                } else {
                    stringResource(Res.string.search_empty_inventory_description)
                },
                actionLabel = if (data.hasItems && data.query.isNotBlank()) stringResource(Res.string.search_clear_query) else null,
                onAction = if (data.hasItems && data.query.isNotBlank()) ({ onQueryChange("") }) else null,
            )
        }
    }
    actionError?.let { item(key = "action-error") { Text(it, color = MaterialTheme.colorScheme.error) } }
    items(data.results, key = { it.item.id }) { record ->
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(AiFinderPalette.Surface, RoundedCornerShape(24.dp))
                .padding(AiFinderSpacing.dp16),
            verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp8)
        ) {
            Row(
                Modifier.fillMaxWidth().clickable { onItemSelected(record.item.id) },
                horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp14),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemPhoto(record.item.photoId, record.item.name, RoundedCornerShape(16.dp),
                    modifier = Modifier.size(88.dp), bounds = record.bounds) {
                    Icon(Icons.Outlined.MyLocation, null, tint = AiFinderPalette.Teal)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp6)) {
                    Text(record.item.name, color = AiFinderPalette.Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(record.path, color = AiFinderPalette.Teal, fontSize = 14.sp)
                    if (record.confirmedAt > 0) Text(
                        stringResource(Res.string.item_confirmed,
                            org.techascent.aifinder.data.subscription.formatPurchaseDate(record.confirmedAt)),
                        color = AiFinderPalette.Muted, fontSize = 12.sp,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp8)) {
                AssistChip(
                    onClick = { onConfirm(record) },
                    enabled = !busy,
                    shape = RoundedCornerShape(50),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = AiFinderPalette.Mint,
                        labelColor = AiFinderPalette.Teal,
                        leadingIconContentColor = AiFinderPalette.Teal,
                    ),
                    border = null,
                    leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(18.dp)) },
                    label = { Text(stringResource(Res.string.item_confirm)) },
                )
                AssistChip(
                    onClick = { onMove(record) },
                    enabled = !busy,
                    shape = RoundedCornerShape(50),
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.DriveFileMove, null, Modifier.size(18.dp)) },
                    label = { Text(stringResource(Res.string.details_move)) },
                )
                AssistChip(
                    onClick = { onItemSelected(record.item.id) },
                    shape = RoundedCornerShape(50),
                    trailingIcon = { Icon(Icons.Outlined.ChevronRight, null, Modifier.size(20.dp)) },
                    label = { Text(stringResource(Res.string.search_details)) },
                )
            }
        }
    }
}
