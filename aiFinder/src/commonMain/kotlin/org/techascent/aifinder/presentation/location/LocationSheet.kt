package org.techascent.aifinder.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.presentation.components.InfoCard
import org.techascent.aifinder.presentation.components.LoadingContent
import org.techascent.aifinder.presentation.components.PrimaryAction
import org.techascent.aifinder.presentation.components.EmptyListCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSheet(viewModel: LocationViewModel, itemCount: Int?, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.start() }
    var creating by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(LocationType.ROOM) }
    var parentId by rememberSaveable { mutableStateOf(state.selectedId) }
    var parentExpanded by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var pendingId by rememberSaveable { mutableStateOf(state.selectedId) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AiFinderPalette.Surface,
        contentColor = AiFinderPalette.Ink,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier.padding(vertical = 16.dp).size(64.dp, 5.dp)
                    .background(AiFinderPalette.Border, RoundedCornerShape(50))
            )
        }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 24.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(if (creating) Res.string.location_create_title else Res.string.location_choose_title),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (creating) stringResource(Res.string.location_create_subtitle)
                else if (itemCount != null) stringResource(Res.string.location_choose_count, itemCount)
                else stringResource(Res.string.location_choose_subtitle),
                color = AiFinderPalette.Muted
            )

            if (creating) {
                Spacer(Modifier.height(4.dp))
                FieldLabel(stringResource(Res.string.location_name))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; viewModel.clearError() },
                    placeholder = { Text(stringResource(Res.string.location_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AiFinderPalette.Background,
                        unfocusedContainerColor = AiFinderPalette.Background,
                        focusedBorderColor = AiFinderPalette.Teal,
                        unfocusedBorderColor = AiFinderPalette.Background,
                        focusedTextColor = AiFinderPalette.Ink,
                        unfocusedTextColor = AiFinderPalette.Ink,
                        cursorColor = AiFinderPalette.Teal
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldLabel(stringResource(Res.string.location_type))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        LocationType.AREA to Res.string.location_area,
                        LocationType.ROOM to Res.string.location_room,
                        LocationType.CONTAINER to Res.string.location_container
                    ).forEach { (option, label) ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option; viewModel.clearError() },
                            label = { Text(stringResource(label), fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(14.dp),
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AiFinderPalette.Surface,
                                labelColor = AiFinderPalette.Muted,
                                selectedContainerColor = AiFinderPalette.Mint,
                                selectedLabelColor = AiFinderPalette.Teal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                FieldLabel(stringResource(Res.string.location_inside))
                Box {
                    InfoCard(
                        title = parentId?.let(state::path) ?: stringResource(Res.string.location_no_parent),
                        subtitle = stringResource(Res.string.location_parent_hint),
                        containerColor = AiFinderPalette.Background,
                        onClick = { parentExpanded = true },
                        trailingContent = {
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = AiFinderPalette.Ink)
                        }
                    )
                    DropdownMenu(
                        expanded = parentExpanded,
                        onDismissRequest = { parentExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp).background(AiFinderPalette.Surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.location_no_parent), color = AiFinderPalette.Ink) },
                            onClick = { parentId = null; parentExpanded = false; viewModel.clearError() }
                        )
                        state.locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(state.path(location.id), color = AiFinderPalette.Ink) },
                                onClick = { parentId = location.id; parentExpanded = false; viewModel.clearError() }
                            )
                        }
                    }
                }
                val preview = listOfNotNull(
                    parentId?.let(state::path),
                    name.trim().takeIf { it.isNotEmpty() } ?: stringResource(Res.string.location_new_name)
                ).joinToString(" › ")
                Text(
                    stringResource(Res.string.location_path_preview, preview),
                    color = AiFinderPalette.Teal,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                        .background(AiFinderPalette.Mint, RoundedCornerShape(16.dp)).padding(16.dp)
                )
                state.createError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                PrimaryAction(
                    stringResource(Res.string.location_create_action),
                    onClick = { viewModel.create(CreateLocationRequest(name, type, parentId), onCreated = onDismiss) },
                    isLoading = state.isCreating,
                    enabled = name.isNotBlank() && !state.isCreating
                )
                TextButton(onClick = { creating = false; viewModel.clearError() }) {
                    Text(stringResource(Res.string.back), color = AiFinderPalette.Teal)
                }
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(Res.string.location_search_hint), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AiFinderPalette.Background,
                        unfocusedContainerColor = AiFinderPalette.Background,
                        focusedBorderColor = AiFinderPalette.Teal,
                        unfocusedBorderColor = AiFinderPalette.Background,
                        focusedTextColor = AiFinderPalette.Ink,
                        unfocusedTextColor = AiFinderPalette.Ink,
                        unfocusedPlaceholderColor = AiFinderPalette.Muted,
                        unfocusedLeadingIconColor = AiFinderPalette.Muted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldLabel(stringResource(Res.string.location_your_locations))
                val matches = state.locations.filter {
                    it.type != LocationType.HOME &&
                        state.path(it.id).contains(query.trim(), ignoreCase = true)
                }
                Column(Modifier.fillMaxWidth().selectableGroup()) {
                    if (state.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            LoadingContent()
                        }
                    }
                    state.loadError?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                        TextButton(onClick = viewModel::retry) {
                            Text(stringResource(Res.string.location_retry), color = AiFinderPalette.Teal, fontWeight = FontWeight.Bold)
                        }
                    }
                    matches.forEach { location ->
                        val selected = pendingId == location.id
                        Row(
                            Modifier.fillMaxWidth()
                                .background(
                                    if (selected) AiFinderPalette.Mint else AiFinderPalette.Surface,
                                    RoundedCornerShape(16.dp)
                                )
                                .selectable(selected, role = Role.RadioButton, onClick = { pendingId = location.id })
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AiFinderPalette.Teal,
                                    unselectedColor = AiFinderPalette.Muted
                                )
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(location.name, color = AiFinderPalette.Ink, fontWeight = FontWeight.Bold)
                                Text(state.path(location.id).removePrefix("Home › "), color = AiFinderPalette.Muted, fontSize = 13.sp)
                            }
                        }
                    }
                    if (matches.isEmpty() && !state.isLoading && state.loadError == null) {
                        EmptyListCard(
                            titleRes = if (query.isBlank()) Res.string.home_spaces_empty_title
                                else Res.string.search_empty_matches_title,
                            descriptionRes = if (query.isBlank()) Res.string.location_empty
                                else Res.string.location_no_matches,
                            actionLabel = if (query.isNotBlank()) stringResource(Res.string.search_clear_query) else null,
                            onAction = if (query.isNotBlank()) ({ query = "" }) else null,
                        )
                    }
                }
                TextButton(
                    onClick = { viewModel.clearError(); parentId = pendingId; creating = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = AiFinderPalette.Mint,
                        contentColor = AiFinderPalette.Teal
                    ),
                    contentPadding = PaddingValues(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.location_create_new),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                PrimaryAction(
                    stringResource(Res.string.location_save_action),
                    onClick = {
                        pendingId?.let { viewModel.select(it); onDismiss() }
                    },
                    enabled = pendingId != null
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = AiFinderPalette.Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
}
