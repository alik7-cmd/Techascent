package org.techascent.composa.checkbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import apphub.composa.generated.resources.Res
import apphub.composa.generated.resources.baseline_check
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.theming.ComposaTheme

/**
 * A customizable composable checkbox component.
 *
 * @param modifier The [Modifier] to be applied to the checkbox.
 * @param checked A boolean value indicating whether the checkbox is checked or not.
 * Defaults to `false`.
 * @param onCheckedChange A callback function triggered when the checkbox state changes.
 * @param borderColor The color of the checkbox border. Defaults to [ComposaColors.GREEN_100].
 * @param tint The tint color for the checkmark icon. Defaults to [Color.Unspecified].
 *
 * This component uses an [IconToggleButton] to handle the toggle behavior and displays a checkmark
 * icon when the checkbox is in the checked state.
 * The checkmark is animated using [AnimatedVisibility]
 * with scale-in and scale-out animations.
 *
 * ### Example
 * ```
 * var checked by remember { mutableStateOf(true) }
 *     ComposaCheckbox(
 *         checked = true,
 *         onCheckedChange = { checked = it },
 *         borderColor = Color.Gray,
 *         tint = Color.Black
 *     )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposaCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: (checked: Boolean) -> Unit,
    borderColor: Color = ComposaTheme.color.backgroundAction,
    tint: Color = ComposaTheme.color.iconNeutralinverse
) {
    IconToggleButton(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
    ) {
        Box(
            modifier = Modifier
                .sizeIn(
                    minWidth = 24.dp,
                    minHeight = 24.dp
                )
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = ComposaTheme.shape.small
                )

        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = checked,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Icon(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(2.dp))
                        .background(color = borderColor),
                    painter = painterResource(resource = Res.drawable.baseline_check),
                    contentDescription = null,
                    tint = tint
                )
            }
        }

    }
}
