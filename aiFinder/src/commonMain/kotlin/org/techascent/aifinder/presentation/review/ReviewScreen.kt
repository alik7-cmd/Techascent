package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.config.AppConfig

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    selectedLocation: String,
    onChooseLocation: () -> Unit,
    onRescanChoice: (String, RescanChoice) -> Unit,
) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedName by rememberSaveable { mutableStateOf("") }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pointedId by remember { mutableStateOf<String?>(null) }
    val detections = (state as? ReviewUiState.Success)?.data?.detections.orEmpty()
    val editing = detections.firstOrNull { it.item.id == editingId }
    val deleting = detections.firstOrNull { it.item.id == deletingId }

    if (editing != null) {
        val saveName: () -> Unit = {
            if (editedName.isNotBlank()) {
                onRename(editing.item.id, editedName)
                editingId = null
            }
        }
        NativeReviewDialog(
            title = stringResource(Res.string.review_rename_title),
            message = stringResource(Res.string.review_name_label),
            confirmLabel = stringResource(Res.string.review_save_name),
            cancelLabel = stringResource(Res.string.review_cancel),
            name = editedName,
            onNameChange = { editedName = it },
            onConfirm = saveName,
            onDismiss = { editingId = null }
        )
    }

    if (deleting != null) {
        NativeReviewDialog(
            title = stringResource(Res.string.review_delete_title),
            message = stringResource(Res.string.review_delete_message, deleting.item.name),
            confirmLabel = stringResource(Res.string.review_delete_confirm),
            cancelLabel = stringResource(Res.string.review_cancel),
            name = null,
            onNameChange = {},
            onConfirm = {
                onDelete(deleting.item.id)
                deletingId = null
            },
            onDismiss = { deletingId = null }
        )
    }

    ScreenLayout(
        topBar = {
            ScreenTopBar(stringResource(Res.string.review_title), onBack)
        },
        bottomBar = {
            if (state is ReviewUiState.Success && detections.isNotEmpty()) {
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.data.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (state.data.saveError != null) {
                        TextButton(onClick = onRetry) { Text(stringResource(Res.string.location_retry)) }
                    }
                    PrimaryAction(
                        if (state.data.isSaving) {
                            stringResource(Res.string.review_saving)
                        } else {
                            stringResource(Res.string.review_save_items, state.data.detections.size)
                        },
                        onSave,
                        enabled = selectedLocation.isNotBlank() && !state.data.isSaving && !state.data.checkingExisting,
                        isLoading = state.data.isSaving || state.data.checkingExisting,
                    )
                }
            }
        }
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.review_intro),
                    color = AiFinderPalette.Muted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            }
        }
        when (state) {
            is ReviewUiState.Loading -> item { LoadingContent() }
            is ReviewUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is ReviewUiState.Success -> {
                state.data.photo?.let { photo ->
                    item(key = "source-photo") {
                        val pointed = detections.firstOrNull { it.item.id == pointedId } ?: detections.firstOrNull()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ItemPhoto(photo.id, pointed?.item?.name, RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                bounds = pointed?.bounds, jpegBytes = photo.jpegBytes,
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                            Text(stringResource(Res.string.review_pointer_hint), color = AiFinderPalette.Muted, fontSize = 13.sp)
                        }
                    }
                }
                item(key = "location") {
                    Surface(
                        onClick = onChooseLocation,
                        enabled = !state.data.isSaving,
                        color = AiFinderPalette.Surface,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, AiFinderPalette.Border),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.LocationOn, null, tint = AiFinderPalette.Teal)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(Res.string.location_save_into), color = AiFinderPalette.Muted, fontSize = 13.sp)
                                Text(
                                    selectedLocation.ifBlank { stringResource(Res.string.review_choose_location) },
                                    color = AiFinderPalette.Ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Icon(Icons.Outlined.ChevronRight, null, tint = AiFinderPalette.Teal)
                        }
                    }
                }
                if (detections.isNotEmpty()) {
                    item(key = "ai-notice") { ReviewResultsNotice() }
                    item(key = "summary") {
                        Text(
                            stringResource(Res.string.review_results_count, detections.size),
                            color = AiFinderPalette.Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(state.data.detections, key = { it.item.id }) { detection ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetectionCard(
                        detection,
                        onEdit = {
                            editedName = detection.item.name
                            editingId = detection.item.id
                        },
                        onDelete = { deletingId = detection.item.id },
                        enabled = !state.data.isSaving,
                    )
                    if (detection.bounds != null && state.data.photo != null) {
                        TextButton(onClick = { pointedId = detection.item.id }) {
                            Text(stringResource(Res.string.review_show_spot), color = AiFinderPalette.Teal)
                        }
                    }
                    if (detection.item.id in state.data.matches) {
                        Text(stringResource(if (detection.item.id in state.data.ambiguousMatches)
                            Res.string.rescan_ambiguous else Res.string.rescan_match),
                            color = AiFinderPalette.Teal, fontSize = 13.sp)
                        Column {
                            RescanChoice.entries.forEach { choice ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (state.data.choices[detection.item.id] ?: RescanChoice.KEEP) == choice,
                                        onClick = { onRescanChoice(detection.item.id, choice) },
                                        enabled = !state.data.isSaving && (choice != RescanChoice.UPDATE ||
                                            detection.item.id !in state.data.ambiguousMatches),
                                        colors = RadioButtonDefaults.colors(selectedColor = AiFinderPalette.Teal),
                                    )
                                    Text(stringResource(when (choice) {
                                        RescanChoice.KEEP -> Res.string.rescan_keep
                                        RescanChoice.UPDATE -> Res.string.rescan_update
                                        RescanChoice.ADD -> Res.string.rescan_add
                                    }), color = AiFinderPalette.Ink, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    }
                }

                if (detections.isEmpty()) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EmptyListCard(
                                titleRes = Res.string.review_empty_title,
                                descriptionRes = Res.string.review_empty_message,
                            )
                            PrimaryAction(stringResource(Res.string.review_scan_again), onBack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewResultsNotice() {
    Surface(color = AiFinderPalette.Mint, shape = RoundedCornerShape(24.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = AiFinderPalette.Teal)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(
                        if (AppConfig.USE_FAKE_ANALYSIS) Res.string.review_sample_title
                        else Res.string.review_ai_title
                    ),
                    color = AiFinderPalette.Teal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(
                        if (AppConfig.USE_FAKE_ANALYSIS) Res.string.review_sample_message
                        else Res.string.review_ai_message
                    ),
                    color = AiFinderPalette.Ink.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
            }
        }
    }
}
