package org.techascent.muslim.tasbeeh.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.message_tasbeeh
import apphub.composeapp.generated.resources.reset_warning_cancel_text
import apphub.composeapp.generated.resources.reset_warning_confirm_text
import apphub.composeapp.generated.resources.reset_warning_message
import apphub.composeapp.generated.resources.reset_warning_title
import apphub.composeapp.generated.resources.title_tasbeeh
import org.jetbrains.compose.resources.StringResource

data class TasbeehUiState(
    val title: StringResource = Res.string.title_tasbeeh,
    val count: Int = 0,
    val infoMessage: StringResource = Res.string.message_tasbeeh,
    val dialogProperty: DialogProperty = DialogProperty(),
    val shouldShowResetDialog: Boolean = false
)

data class DialogProperty(
    val title: StringResource =  Res.string.reset_warning_title,
    val message: StringResource = Res.string.reset_warning_message,
    val confirmText: StringResource = Res.string.reset_warning_confirm_text,
    val cancelText: StringResource = Res.string.reset_warning_cancel_text
)