package org.techascent.aifinder.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
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
    onRetry: () -> Unit
) {
    ScreenLayout(
        topBar = {
            Surface(color = AiFinderPalette.Background) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    BackButton(onBack)
                }
            }
        }
    ) {
        item(key = "title") { SectionTitle(stringResource(Res.string.search_title)) }
        when (state) {
            is SearchUiState.Loading -> searchLoadingContent()
            is SearchUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is SearchUiState.Success -> searchContent(state.data, onQueryChange, onItemSelected)
        }
    }
}

private fun LazyListScope.searchContent(
    data: SearchContent,
    onQueryChange: (String) -> Unit,
    onItemSelected: (String) -> Unit
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = ComposaTheme.typography.caption,
            color = AiFinderPalette.Muted
        )
    }
    if (data.results.isNotEmpty()) item(key = "count") {
        Text(
            stringResource(
                if (data.results.size == 1) Res.string.search_one_found else Res.string.search_items_found,
                data.results.size
            ),
            color = AiFinderPalette.Muted,
            modifier = Modifier.padding(vertical = 8.dp)
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
    items(data.results, key = { it.item.id }) { record ->
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(AiFinderPalette.Surface, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ItemPhoto(
                photoId = record.item.photoId,
                contentDescription = record.item.name,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                Text(
                    text = if (record.item.name.contains("HDMI", ignoreCase = true)) {
                        "HDMI"
                    } else {
                        record.item.category
                    },
                    modifier = Modifier.background(
                        AiFinderPalette.Teal.copy(alpha = 0.3f),
                        RoundedCornerShape(28.dp)
                    ).padding(horizontal = 48.dp, vertical = 22.dp),
                    color = AiFinderPalette.Teal,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SectionTitle(record.item.name)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.MyLocation,
                    contentDescription = null,
                    tint = AiFinderPalette.Muted,
                    modifier = Modifier.size(16.dp)
                )
                Text(record.path, color = AiFinderPalette.Muted, fontSize = 14.sp)
            }
            PrimaryAction(
                stringResource(Res.string.search_view_location),
                onClick = { onItemSelected(record.item.id) }
            )
        }
    }
}
