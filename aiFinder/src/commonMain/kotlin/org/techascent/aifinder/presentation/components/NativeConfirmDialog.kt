package org.techascent.aifinder.presentation.components

import androidx.compose.runtime.Composable
import org.techascent.aifinder.presentation.review.NativeReviewDialog

/** Destructive confirmation shown as a Material dialog on Android and a UIAlertController on iOS. */
@Composable
internal fun NativeConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) = NativeReviewDialog(
    title = title,
    message = message,
    confirmLabel = confirmLabel,
    cancelLabel = cancelLabel,
    name = null,
    onNameChange = {},
    onConfirm = onConfirm,
    onDismiss = onDismiss,
)
