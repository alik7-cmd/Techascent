package org.techascent.aifinder.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DeleteProfileSheet(
    busy: Boolean,
    failed: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val deleting by rememberUpdatedState(busy)
    val windowHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !deleting || it != SheetValue.Hidden },
    )
    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = sheetState,
        containerColor = AiFinderPalette.Surface,
        sheetGesturesEnabled = false,
        dragHandle = null,
    ) {
        Column(
            Modifier.fillMaxWidth().height(windowHeight * 0.85f),
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(Res.string.account_delete_title),
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AiFinderPalette.Ink,
                )
                Text(stringResource(Res.string.account_delete_message))
                Text(stringResource(Res.string.account_delete_purchases))
                Text(stringResource(Res.string.account_delete_copies))
                Row(
                    Modifier.fillMaxWidth().toggleable(
                        value = acknowledged,
                        enabled = !busy,
                        role = Role.Checkbox,
                        onValueChange = { acknowledged = it },
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = acknowledged, onCheckedChange = null, enabled = !busy)
                    Text(
                        stringResource(Res.string.account_delete_acknowledge),
                        modifier = Modifier.padding(start = 12.dp).weight(1f),
                    )
                }
            }
            HorizontalDivider(color = AiFinderPalette.Border)
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (failed) {
                    Text(
                        stringResource(Res.string.account_delete_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = onConfirm,
                    enabled = acknowledged && !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(stringResource(
                        when {
                            busy -> Res.string.account_delete_loading
                            failed -> Res.string.account_delete_retry
                            else -> Res.string.account_delete_confirm
                        }
                    ))
                }
                TextButton(
                    onClick = onDismiss,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.account_delete_cancel))
                }
            }
        }
    }
}
