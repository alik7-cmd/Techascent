package org.techascent.aifinder.presentation.review

import androidx.compose.runtime.Composable

/** A null [name] displays a destructive confirmation instead of a text editor. */
@Composable
internal expect fun NativeReviewDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    name: String?,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
