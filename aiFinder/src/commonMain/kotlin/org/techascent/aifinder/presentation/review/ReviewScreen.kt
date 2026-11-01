package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    selectedLocation: String,
    onChooseLocation: () -> Unit
) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedName by rememberSaveable { mutableStateOf("") }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
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
            Surface(color = AiFinderPalette.Background) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    BackButton(onBack)
                }
            }
        },
        bottomBar = {
            if (state is ReviewUiState.Success && detections.isNotEmpty()) {
                Box(
                    Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    PrimaryAction(
                        stringResource(Res.string.review_save_items, state.data.detections.size),
                        onSave,
                        enabled = selectedLocation.isNotBlank()
                    )
                }
            }
        }
    ) {
        item(key = "header") { SectionTitle(stringResource(Res.string.review_title)) }
        when (state) {
            is ReviewUiState.Loading -> item { LoadingContent() }
            is ReviewUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is ReviewUiState.Success -> {
                item(key = "location") {
                    InfoCard(
                        title = stringResource(Res.string.location_save_into),
                        subtitle = selectedLocation,
                        onClick = onChooseLocation
                    )
                }
                item(key = "summary") {
                    InfoCard(
                        title = stringResource(Res.string.review_items_found, state.data.detections.size),
                        subtitle = stringResource(Res.string.review_mock_notice),
                        containerColor = AiFinderPalette.Mint,
                        titleColor = AiFinderPalette.Teal,
                        titleFontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                items(state.data.detections, key = { it.item.id }) { detection ->
                    DetectionCard(
                        detection,
                        onEdit = {
                            editedName = detection.item.name
                            editingId = detection.item.id
                        },
                        onDelete = { deletingId = detection.item.id }
                    )
                }
                if (detections.isEmpty()) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoCard(
                                title = stringResource(Res.string.review_empty_title),
                                subtitle = stringResource(Res.string.review_empty_message)
                            )
                            PrimaryAction(stringResource(Res.string.review_scan_again), onBack)
                        }
                    }
                }
            }
        }
    }
}
