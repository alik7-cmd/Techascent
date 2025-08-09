package org.techascent.composa.button.primary

import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.techascent.composa.button.ComposaButtonDefaults
import org.techascent.composa.common.DrawableData


/**
 * ComposaButton is a customizable button component that supports different visual states
 * (Idle, Success, Failed),
 * optional start and end icons, and size variations.
 *
 * This component is useful for actions that might need to reflect progress or result states,
 * like form submission or approval buttons.
 *
 * @param text The text label displayed on the button.
 * @param onClick The lambda function triggered when the button is clicked.
 * @param modifier The [Modifier] to be applied to the button.
 * @param leftIcon Optional [DrawableData] representing an icon placed to the left of the text.
 * @param rightIcon Optional [DrawableData] representing an icon placed to the right of the text.
 * @param isEnabled Controls the enabled/disabled state of the button. Defaults to `true`.
 * @param isSmall When set to `true`, renders a smaller version of the button.
 * @param colors [ButtonColors] to customize the background and content colors.
 * @param iconTint The color to apply to icons (left, right, and state-specific icons).
 * @param buttonState Represents the state of the button (Idle, Success, Failed).
 * Affects the appearance and behavior.
 *
 *
 * ### Example
 * ```
 * ComposaButton(
 *     text = "Submit",
 *     onClick = { /* handle your action */ },
 *     leftIcon = DrawableData(
 *         imageRes = R.drawable.baseline_check_24,
 *         tint = Color.White
 *     ),
 *     isSmall = false,
 *     isEnabled = true,
 *     buttonState = ButtonState.Idle,
 *     iconTint = Color.White
 * )
 * ```
 * @see ButtonState
 * @see DrawableData
 */
@Composable
fun ComposaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leftIcon: DrawableData? = null,
    rightIcon: DrawableData? = null,
    isEnabled: Boolean = true,
    isSmall: Boolean = false,
    colors: ButtonColors = ComposaButtonDefaults.composaButtonColor(),
    iconTint: Color,
    buttonState: ButtonState = ButtonState.Idle,
) {
    ComposaButtonInternal(
        onClick = onClick,
        modifier = modifier,
        isSmall = isSmall,
        text = text,
        isEnabled = isEnabled,
        buttonState = buttonState,
        leftIcon = leftIcon,
        rightIcon = rightIcon,
        colors = colors,
        iconTint = iconTint
    )

}



