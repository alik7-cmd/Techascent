package org.techascent.composa.button.text

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import org.techascent.composa.button.ButtonContent
import org.techascent.composa.button.ComposaButtonDefaults
import org.techascent.composa.button.primary.ButtonState
import org.techascent.composa.button.primary.ComposaButtonAnimatedContent
import org.techascent.composa.button.primary.TransformButton
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.buttonDefaultMinSize
import org.techascent.composa.common.contentPadding
import org.techascent.composa.theming.ComposaTheme

@Composable
internal fun ComposaButtonTextPrivate(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors,
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
    val iconTint = ComposaButtonDefaults.textButtonIconColor(buttonState, isEnabled)
    Button(
        onClick = onClick,
        modifier = modifier
            .buttonDefaultMinSize(isSmall)
            .widthIn(max = ComposaButtonDefaults.TEXT_BUTTON_MAX_WIDTH)
            .semantics { contentDescription = text },
        enabled = isEnabled,
        colors = colors,
        border = null,
        contentPadding = contentPadding,
        elevation = ComposaButtonDefaults.buttonElevation(),
        shape = ComposaTheme.shape.medium,
        content = {
            ComposaButtonAnimatedContent(buttonState) { targetExpanded ->
                if (targetExpanded) {
                    ButtonContent(
                        isSmall = isSmall,
                        isEmphasized = isEmphasized,
                        text = text,
                        textAlign = textAlign,
                        iconLeft = leftIcon?.copy(
                            tint = iconTint
                        ),
                        iconRight = rightIcon?.copy(
                            tint = iconTint
                        )
                    )
                } else {
                    TransformButton(
                        buttonState = buttonState,
                        color = ComposaTheme.color.componentButtonIconAction
                    )
                }
            }
        }

    )

}
