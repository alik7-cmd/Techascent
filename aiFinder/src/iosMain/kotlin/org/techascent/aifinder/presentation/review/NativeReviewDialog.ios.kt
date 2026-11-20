package org.techascent.aifinder.presentation.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UITextField
import platform.UIKit.UITextFieldTextDidChangeNotification

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
    val controller = LocalUIViewController.current
    val currentNameChange = rememberUpdatedState(onNameChange)
    val currentConfirm = rememberUpdatedState(onConfirm)
    val currentDismiss = rememberUpdatedState(onDismiss)
    val editing = name != null

    DisposableEffect(controller, title, message, confirmLabel, cancelLabel, editing) {
        val alert = UIAlertController.alertControllerWithTitle(
            title, if (editing) null else message, UIAlertControllerStyleAlert
        )
        val confirm = UIAlertAction.actionWithTitle(
            confirmLabel,
            if (editing) UIAlertActionStyleDefault else UIAlertActionStyleDestructive
        ) { currentConfirm.value() }
        var editor: UITextField? = null
        if (editing) {
            alert.addTextFieldWithConfigurationHandler { field ->
                editor = field
                field?.text = name
                field?.placeholder = message
            }
            confirm.enabled = !name.isNullOrBlank()
        }
        val observer = editor?.let { field ->
            NSNotificationCenter.defaultCenter.addObserverForName(
                UITextFieldTextDidChangeNotification, field, null
            ) {
                val value = field.text.orEmpty()
                currentNameChange.value(value)
                confirm.enabled = value.isNotBlank()
            }
        }
        alert.addAction(confirm)
        alert.addAction(UIAlertAction.actionWithTitle(cancelLabel, UIAlertActionStyleCancel) {
            currentDismiss.value()
        })
        controller.presentViewController(alert, animated = true, completion = null)
        onDispose {
            observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            alert.dismissViewControllerAnimated(false, completion = null)
        }
    }
}
