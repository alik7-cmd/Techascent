package org.techascent.aifinder.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun DetailsScreen(
    state: DetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    onDeletePhoto: () -> Unit,
    busy: Boolean,
    actionError: String?,
) {
    var confirmingDelete by remember { mutableStateOf(false) }
    var confirmingPhotoDelete by remember { mutableStateOf(false) }
    val entry = (state as? DetailsUiState.Success)?.data?.entry
    if (confirmingDelete) {
        NativeConfirmDialog(
            title = stringResource(Res.string.item_delete),
            message = stringResource(Res.string.item_delete_message),
            confirmLabel = stringResource(Res.string.item_delete),
            cancelLabel = stringResource(Res.string.account_logout_cancel),
            onConfirm = { confirmingDelete = false; onDelete() },
            onDismiss = { confirmingDelete = false },
        )
    }
    if (confirmingPhotoDelete) {
        NativeConfirmDialog(
            title = stringResource(Res.string.photo_delete),
            message = stringResource(Res.string.photo_delete_message),
            confirmLabel = stringResource(Res.string.photo_delete),
            cancelLabel = stringResource(Res.string.account_logout_cancel),
            onConfirm = { confirmingPhotoDelete = false; onDeletePhoto() },
            onDismiss = { confirmingPhotoDelete = false },
        )
    }

    ScreenLayout(
        topBar = { ScreenTopBar(stringResource(Res.string.details_title), onBack) },
        bottomBar = {
            if (entry != null) {
                Surface(color = AiFinderPalette.Background) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onMove,
                            enabled = !busy,
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AiFinderPalette.Teal),
                        ) {
                            Text(stringResource(Res.string.details_move), fontWeight = FontWeight.SemiBold)
                        }
                        PrimaryAction(
                            stringResource(Res.string.details_edit),
                            onClick = onEdit,
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) {
        when (state) {
            is DetailsUiState.Loading -> item { LoadingContent() }
            is DetailsUiState.Error -> item {
                ErrorCard(
                    title = stringResource(Res.string.details_error_title),
                    message = state.message,
                    onRetry = onRetry,
                )
            }
            is DetailsUiState.Success -> {
                if (entry == null) {
                    item {
                        EmptyListCard(
                            titleRes = Res.string.details_not_found,
                            descriptionRes = Res.string.details_missing_message,
                        )
                    }
                } else {
                    item(key = "identity") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                entry.item.name,
                                color = AiFinderPalette.Ink,
                                fontSize = 28.sp,
                                lineHeight = 34.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Surface(color = AiFinderPalette.Mint, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    entry.item.category,
                                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = AiFinderPalette.Teal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    item(key = "preview") {
                        Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                            Column(Modifier.fillMaxWidth()) {
                                ItemPhoto(
                                    photoId = entry.item.photoId,
                                    contentDescription = stringResource(Res.string.details_photo_description, entry.item.name),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    bounds = entry.bounds,
                                    contentScale = ContentScale.Fit,
                                    matchImageAspectRatio = true,
                                ) {
                                    Column(
                                        Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image, null,
                                            tint = AiFinderPalette.Teal, modifier = Modifier.size(40.dp),
                                        )
                                        Text(
                                            stringResource(Res.string.details_photo_placeholder),
                                            color = AiFinderPalette.Teal,
                                            fontSize = 14.sp,
                                        )
                                        if (entry.bounds != null) {
                                            Text(stringResource(Res.string.item_marker), color = AiFinderPalette.Muted, fontSize = 12.sp)
                                        }
                                    }
                                }
                                Text(
                                    stringResource(Res.string.details_source_photo),
                                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    color = AiFinderPalette.Muted,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    item(key = "location") {
                        val path = entry.path.split("›").map(String::trim).filter(String::isNotEmpty)
                            .ifEmpty { listOf(entry.location.name) }
                        Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                            Column(
                                Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = AiFinderPalette.Teal)
                                    Text(
                                        stringResource(Res.string.details_location_heading),
                                        color = AiFinderPalette.Ink,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                path.forEachIndexed { index, name ->
                                    if (index > 0) {
                                        Icon(
                                            Icons.Outlined.ArrowDownward, null,
                                            tint = AiFinderPalette.Teal,
                                            modifier = Modifier.padding(start = 4.dp).size(20.dp),
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            Modifier.size(28.dp).background(AiFinderPalette.Mint, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("${index + 1}", color = AiFinderPalette.Teal, fontSize = 12.sp)
                                        }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                stringResource(if (index == 0) Res.string.location_go_to else Res.string.location_then_look),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                            Text(
                                                name,
                                                color = if (index == path.lastIndex) AiFinderPalette.Teal else AiFinderPalette.Ink,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                                Text(
                                    if (entry.confirmedAt > 0) stringResource(
                                        Res.string.item_confirmed,
                                        org.techascent.aifinder.data.subscription.formatPurchaseDate(entry.confirmedAt),
                                    ) else stringResource(Res.string.item_confirmed_unknown),
                                    color = AiFinderPalette.Muted,
                                    fontSize = 13.sp,
                                )
                                FilledTonalButton(
                                    onClick = onConfirm,
                                    enabled = !busy,
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = AiFinderPalette.Mint,
                                        contentColor = AiFinderPalette.Teal,
                                    ),
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(Res.string.item_confirm),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                    item(key = "item-actions") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            if (entry.item.photoId != null) {
                                OutlinedButton(
                                    onClick = { confirmingPhotoDelete = true },
                                    enabled = !busy,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                    ),
                                ) {
                                    Icon(Icons.Outlined.HideImage, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(stringResource(Res.string.photo_delete))
                                }
                                Text(
                                    stringResource(Res.string.photo_delete_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            FilledTonalButton(
                                onClick = { confirmingDelete = true },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(Res.string.item_delete), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (entry.item.description.isNotBlank()) {
                        item(key = "description") {
                            Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        stringResource(Res.string.details_notes),
                                        color = AiFinderPalette.Ink,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                    )
                                    Text(entry.item.description, color = AiFinderPalette.Muted, fontSize = 15.sp, lineHeight = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
