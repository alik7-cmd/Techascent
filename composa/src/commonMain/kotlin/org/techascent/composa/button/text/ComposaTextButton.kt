package org.techascent.composa.button.text

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import org.techascent.composa.button.primary.ButtonState
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.contentPadding
import org.techascent.composa.theming.ComposaTheme

@Composable
fun ComposaButtonText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    isEnabled: Boolean = true,
    isSmall: Boolean = false,
    isEmphasized: Boolean = false,
    leftIcon: DrawableData? = null,
    rightIcon: DrawableData? = null,
    buttonState: ButtonState = ButtonState.Idle,
    contentPadding: PaddingValues = contentPadding(
        buttonState = buttonState,
        isSmall = isSmall
    )
) {
    ComposaButtonTextPrivate(
        text = text,
        onClick = onClick,
        modifier = modifier,
        colors = textButtonColor(),
        textAlign = textAlign,
        isEnabled = isEnabled,
        isSmall = isSmall,
        isEmphasized = isEmphasized,
        leftIcon = leftIcon,
        rightIcon = rightIcon,
        buttonState = buttonState,
        contentPadding = contentPadding
    )

}

@Composable
private fun textButtonColor(
    containerColor: Color = Color.Transparent,
    contentColor: Color = ComposaTheme.color.componentButtonTextAction,
    disabledContentColor: Color = ComposaTheme.color.componentButtonTextActionDisabled
) = ButtonDefaults.textButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContentColor = disabledContentColor

)
