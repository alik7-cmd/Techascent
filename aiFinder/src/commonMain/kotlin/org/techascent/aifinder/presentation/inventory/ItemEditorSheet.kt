package org.techascent.aifinder.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.presentation.components.PrimaryAction
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.location.LocationUiState
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(ExperimentalMaterial3Api::class, kotlin.uuid.ExperimentalUuidApi::class)
@Composable
fun ItemEditorSheet(
    entry: LocatedItem?,
    initialLocationId: String?,
    locations: LocationUiState,
    action: InventoryActionState,
    moveOnly: Boolean,
    onSave: (InventoryItem, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(entry?.item?.name.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(entry?.item?.category ?: "General") }
    var notes by rememberSaveable { mutableStateOf(entry?.item?.description.orEmpty()) }
    var aliases by rememberSaveable { mutableStateOf(entry?.item?.tags?.joinToString(", ").orEmpty()) }
    var locationId by rememberSaveable { mutableStateOf(entry?.location?.id ?: initialLocationId) }
    var expanded by remember { mutableStateOf(false) }
    val availablePlaces = locations.locations.filter { it.type != LocationType.HOME }
    val canChoosePlace = !action.busy && !locations.isLoading && availablePlaces.isNotEmpty()
    val draftId = rememberSaveable { kotlin.uuid.Uuid.random().toString() }
    ModalBottomSheet(
        onDismissRequest = { if (!action.busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AiFinderPalette.Surface,
        contentColor = AiFinderPalette.Ink,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionTitle(stringResource(when {
                moveOnly -> Res.string.details_move
                entry != null -> Res.string.details_edit
                else -> Res.string.item_add
            }))
            if (!moveOnly) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(Res.string.item_name)) },
                    singleLine = true, enabled = !action.busy, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text(stringResource(Res.string.item_category)) },
                    singleLine = true, enabled = !action.busy, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(aliases, { aliases = it }, label = { Text(stringResource(Res.string.item_aliases)) },
                    supportingText = { Text(stringResource(Res.string.item_aliases_hint)) },
                    enabled = !action.busy, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(Res.string.details_notes)) },
                    enabled = !action.busy, modifier = Modifier.fillMaxWidth())
            }
            if (moveOnly && entry != null) {
                Text(stringResource(Res.string.item_move_intro, entry.item.name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ExposedDropdownMenuBox(
                expanded = expanded && canChoosePlace,
                onExpandedChange = { if (canChoosePlace) expanded = it },
            ) {
                OutlinedTextField(
                    value = locationId?.takeIf { id -> availablePlaces.any { it.id == id } }
                        ?.let(locations::path).orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = canChoosePlace,
                    label = { Text(stringResource(Res.string.place_where)) },
                    placeholder = { Text(stringResource(Res.string.location_choose_title)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && canChoosePlace) },
                    supportingText = {
                        Text(stringResource(when {
                            locations.isLoading -> Res.string.item_places_loading
                            availablePlaces.isEmpty() && locations.loadError == null -> Res.string.item_places_empty
                            else -> Res.string.item_place_picker_hint
                        }))
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = canChoosePlace,
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expanded && canChoosePlace,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 280.dp),
                ) {
                    availablePlaces.sortedBy { locations.path(it.id).lowercase() }.forEach { place ->
                        DropdownMenuItem(
                            text = {
                                Text(locations.path(place.id),
                                    color = if (place.id == locationId) AiFinderPalette.Teal else AiFinderPalette.Ink)
                            },
                            onClick = { locationId = place.id; expanded = false },
                        )
                    }
                }
            }
            locations.loadError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            action.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            PrimaryAction(
                stringResource(if (moveOnly) Res.string.details_move else Res.string.item_save),
                onClick = {
                    val id = locationId ?: return@PrimaryAction
                    onSave(
                        (entry?.item ?: InventoryItem(draftId, "", "")).copy(
                            name = name.trim(), category = category.trim().ifBlank { "General" },
                            description = notes.trim(),
                            tags = aliases.split(",").map(String::trim).filter(String::isNotBlank).distinct(),
                        ), id,
                    )
                },
                enabled = name.isNotBlank() && availablePlaces.any { it.id == locationId },
                isLoading = action.busy,
            )
        }
    }
}
