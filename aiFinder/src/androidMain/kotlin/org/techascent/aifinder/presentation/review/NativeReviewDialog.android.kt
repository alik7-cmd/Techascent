package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction

@Composable
internal actual fun NativeReviewDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    name: String?,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val canConfirm = name == null || name.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (name != null) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(message) },
                    singleLine = true,
                    isError = name.isBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canConfirm) onConfirm() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            } else {
                Text(message)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canConfirm) {
                if (name == null) {
                    Text(confirmLabel, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(confirmLabel)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cancelLabel) } }
    )
    if (name != null) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}
