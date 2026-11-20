package org.techascent.aifinder.presentation.location

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.presentation.components.InfoCard
import org.techascent.aifinder.presentation.components.ShimmerBlock
import org.techascent.aifinder.presentation.components.PrimaryAction
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSheet(
    viewModel: LocationViewModel,
    itemCount: Int?,
    onDismiss: () -> Unit,
    startCreating: Boolean = false,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.start() }
    var browsingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(startCreating) }
    var showingHelp by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    val current = state.locations.firstOrNull { it.id == browsingId }
    val path = state.ancestors(browsingId).joinToString(" › ") { it.name }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val backLabel = stringResource(Res.string.back)
    LaunchedEffect(browsingId, state.locations, state.isLoading) {
        if (!state.isLoading && browsingId != null && current == null) browsingId = null
    }

    ModalBottomSheet(
        onDismissRequest = { if (!state.isCreating) onDismiss() },
        sheetState = sheetState,
        containerColor = AiFinderPalette.Surface,
        contentColor = AiFinderPalette.Ink,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = AiFinderSpacing.dp24).padding(bottom = AiFinderSpacing.dp24).imePadding(),
            verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16),
        ) {
            Text(
                stringResource(when {
                    showingHelp -> Res.string.place_help_title
                    creating -> Res.string.place_add_title
                    else -> Res.string.location_choose_title
                }),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            if (showingHelp) {
                PlaceGuide()
                PrimaryAction(stringResource(Res.string.place_help_done), { showingHelp = false })
            } else {
                Text(
                    stringResource(if (creating) Res.string.place_add_subtitle else Res.string.location_choose_subtitle),
                    color = AiFinderPalette.Muted,
                )
                TextButton(onClick = { showingHelp = true }, enabled = !state.isCreating) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AiFinderPalette.Teal,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(AiFinderSpacing.dp8))
                    Text(stringResource(Res.string.place_help_action), color = AiFinderPalette.Teal)
                }
                if (creating) {
                    InfoCard(
                        title = stringResource(Res.string.place_where),
                        subtitle = path.ifBlank { stringResource(Res.string.place_standalone) },
                        containerColor = AiFinderPalette.Mint,
                    )
                    Text(stringResource(Res.string.place_name), fontWeight = FontWeight.SemiBold)
                    PlaceTextField(
                        name, { name = it; viewModel.clearError() },
                        stringResource(Res.string.place_name_hint),
                        enabled = !state.isCreating,
                    )
                    val preview = listOf(path, name.trim()).filter { it.isNotBlank() }.joinToString(" › ")
                    if (name.isNotBlank()) {
                        InfoCard(
                            title = stringResource(Res.string.place_preview),
                            subtitle = preview,
                            containerColor = AiFinderPalette.Mint,
                        )
                    }
                    state.createError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    state.loadError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::retry) {
                            Text(stringResource(Res.string.location_retry), color = AiFinderPalette.Teal)
                        }
                    }
                    PrimaryAction(
                        stringResource(if (startCreating) Res.string.place_add_only else Res.string.place_add_use),
                        onClick = {
                            viewModel.create(
                                CreateLocationRequest(
                                    name,
                                    if (browsingId == null) LocationType.ROOM else LocationType.CONTAINER,
                                    browsingId,
                                ),
                                onCreated = onDismiss,
                            )
                        },
                        isLoading = state.isCreating,
                        enabled = !state.isLoading && state.loadError == null &&
                            name.isNotBlank() && (browsingId == null || current != null),
                    )
                    TextButton(
                        onClick = { creating = false; viewModel.clearError() },
                        enabled = !state.isCreating,
                    ) { Text(backLabel, color = AiFinderPalette.Teal) }
                } else {
                    if (browsingId == null && query.isBlank()) state.selectedLocation?.let {
                        InfoCard(
                            title = stringResource(Res.string.place_current_selection),
                            subtitle = state.selectedPath,
                            containerColor = AiFinderPalette.Mint,
                            onClick = { browsingId = it.id },
                        )
                    }
                    PlaceTextField(query, { query = it }, stringResource(Res.string.location_search_hint), search = true)
                    if (query.isBlank()) {
                        // Each ancestor is a separate action, so users can return to any level.
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { browsingId = null }) {
                                Text(stringResource(Res.string.place_all), color = AiFinderPalette.Teal)
                            }
                            state.ancestors(browsingId).forEach { ancestor ->
                                Text("›", color = AiFinderPalette.Muted)
                                TextButton(onClick = { browsingId = ancestor.id }) {
                                    Text(ancestor.name, color = AiFinderPalette.Teal)
                                }
                            }
                        }
                    }
                    if (state.isLoading) repeat(3) {
                        ShimmerBlock(Modifier.fillMaxWidth().height(64.dp))
                    }
                    state.loadError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::retry) {
                            Text(stringResource(Res.string.location_retry), color = AiFinderPalette.Teal)
                        }
                    }
                    if (current != null && query.isBlank()) {
                        InfoCard(
                            title = stringResource(Res.string.place_preview),
                            subtitle = path,
                            containerColor = AiFinderPalette.Mint,
                        )
                        PrimaryAction(
                            stringResource(Res.string.place_use, current.name),
                            onClick = { viewModel.select(current.id); onDismiss() },
                            enabled = !state.isLoading && state.loadError == null,
                        )
                    }
                    val matches = if (query.isBlank()) state.placesInside(browsingId) else {
                        state.locations.filter {
                            it.type != LocationType.HOME && state.path(it.id).contains(query.trim(), ignoreCase = true)
                        }.sortedBy { it.name.lowercase() }
                    }
                    matches.forEach { place ->
                        InfoCard(
                            title = place.name,
                            subtitle = if (query.isNotBlank()) state.path(place.id)
                            else stringResource(Res.string.place_open),
                            containerColor = AiFinderPalette.Background,
                            leadingContent = {
                                Box(
                                    Modifier.size(40.dp).background(AiFinderPalette.Mint, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        if (state.ancestors(place.id).size == 1) Icons.Outlined.Home
                                        else Icons.Outlined.Inventory2,
                                        contentDescription = null,
                                        tint = AiFinderPalette.Teal,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            },
                            onClick = { browsingId = place.id; query = "" },
                            trailingContent = {
                                Icon(
                                    Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = AiFinderPalette.Muted,
                                )
                            },
                        )
                    }
                    if (matches.isEmpty() && !state.isLoading && state.loadError == null) {
                        Text(
                            stringResource(when {
                                query.isNotBlank() -> Res.string.location_no_matches
                                current != null -> Res.string.place_empty_inside
                                else -> Res.string.place_empty
                            }),
                            color = AiFinderPalette.Muted,
                        )
                    }
                    if (query.isNotBlank()) {
                        TextButton(onClick = { query = "" }) {
                            Text(stringResource(Res.string.search_clear_query), color = AiFinderPalette.Teal)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { name = ""; viewModel.clearError(); creating = true },
                            enabled = !state.isLoading && state.loadError == null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                if (current == null) stringResource(Res.string.place_add_title)
                                else stringResource(Res.string.place_add_inside, current.name),
                                color = AiFinderPalette.Teal,
                            )
                        }
                        if (current == null && itemCount != null) {
                            Text(stringResource(Res.string.location_choose_count, itemCount), color = AiFinderPalette.Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    enabled: Boolean = true,
    search: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(hint) },
        leadingIcon = if (search) ({ Icon(Icons.Outlined.Search, contentDescription = null) }) else null,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AiFinderPalette.Ink,
            unfocusedTextColor = AiFinderPalette.Ink,
            focusedBorderColor = AiFinderPalette.Teal,
            unfocusedBorderColor = AiFinderPalette.Border,
            focusedContainerColor = AiFinderPalette.Background,
            unfocusedContainerColor = AiFinderPalette.Background,
            cursorColor = AiFinderPalette.Teal,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PlaceGuide() {
    Text(stringResource(Res.string.place_help_intro), color = AiFinderPalette.Muted)
    Column(
        Modifier.fillMaxWidth().background(AiFinderPalette.Mint, RoundedCornerShape(20.dp)).padding(AiFinderSpacing.dp16),
        verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp10),
    ) {
        listOf(
            Res.string.place_example_room to Icons.Outlined.Home,
            Res.string.place_example_wardrobe to Icons.Outlined.Checkroom,
            Res.string.place_example_drawer to Icons.Outlined.Inventory2,
        ).forEachIndexed { index, (label, icon) ->
            Row(
                Modifier.padding(start = (index * 18).dp)
                    .background(AiFinderPalette.Surface, RoundedCornerShape(12.dp)).padding(AiFinderSpacing.dp12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp10),
            ) {
                Icon(icon, contentDescription = null, tint = AiFinderPalette.Teal)
                Text(stringResource(label), color = AiFinderPalette.Ink, fontWeight = FontWeight.SemiBold)
            }
            if (index < 2) Text(
                "↓",
                modifier = Modifier.padding(start = AiFinderSpacing.dp20 + AiFinderSpacing.dp18 * index.toFloat()),
                color = AiFinderPalette.Teal,
            )
        }
        Text(stringResource(Res.string.place_help_items), color = AiFinderPalette.Teal, fontWeight = FontWeight.SemiBold)
    }
    Text(stringResource(Res.string.place_help_optional), color = AiFinderPalette.Ink)
    Text(stringResource(Res.string.place_help_steps), color = AiFinderPalette.Muted)
}
