package org.techascent.composa.button

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.techascent.composa.button.primary.ButtonState
import org.techascent.composa.theming.ComposaTheme

object ComposaButtonDefaults {

    val TEXT_BUTTON_MAX_WIDTH = 300.dp

    @Composable
    fun textButtonIconColor(
        buttonState: ButtonState,
        isEnable: Boolean,
    ): Color {
        val isLoading = buttonState == ButtonState.Loading
        return if (!isEnable && !isLoading) {
            ComposaTheme.color.componentButtonIconActionDisabled
        } else {
            ComposaTheme.color.componentButtonIconAction
        }
    }

    @Composable
    fun buttonElevation() = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)

    @Composable
    fun composaButtonColor() = ButtonDefaults.buttonColors(
        contentColor = ComposaTheme.color.componentButtonTextNeutralInverse,
        containerColor = ComposaTheme.color.componentButtonBackgroundAction,
        disabledContentColor = ComposaTheme.color.componentButtonTextNeutralInverse,
        disabledContainerColor = ComposaTheme.color.componentButtonBackgroundActionDisabled
    )
}
