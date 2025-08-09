package org.techascent.composa.switch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.orDefaultTint
import org.techascent.composa.theming.ComposaTheme

/**
 * A customizable Switch composable with an optional checkmark icon in the thumb when checked.
 * This composable wraps the Material 3 [Switch] and has an option to add animated checkmark icon
 * that appears when the switch is in the checked state.
 *
 * @param modifier [Modifier] to be applied to the switch.
 * @param checked The current checked state of the switch.
 * @param onCheckedChange The callback to be invoked when the checked state changes.
 * @param colors [SwitchColors] to customize the colors of the switch in different states.
 *   Defaults to [SwitchDefaults.colors()].
 * @param enabled Controls the enabled state of the switch. When `false`, the switch is
 *   disabled and does not respond to user interactions. Defaults to `true`.
 * @param showIconWhenChecked Whether to show the checkmark icon when the switch is checked.
 *   Defaults to `true`
 * @param iconRes The resource ID of the checkmark icon to be displayed when the switch is checked.
 * @param iconTint The tint color applied to the checkmark icon when the switch is checked.
 *   Defaults to [Color.Unspecified].
 *
 * ### Example
 * ```
 * ComposaSwitch(
 *         checked = true,
 *         onCheckedChange = {},
 *         enabled = true,
 *         showIconWhenChecked = false,
 *         iconTint = Color.Black
 *     )
 * ```
 */
@Composable
fun ComposaSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((checked: Boolean) -> Unit)?,
    colors: SwitchColors = defaultSwitchColors(),
    enabled: Boolean = true,
    showIconWhenChecked: Boolean = true,
    iconRes: DrawableData? = null,
) {

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        thumbContent = {
            AnimatedVisibility(
                visible = checked && showIconWhenChecked && iconRes != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Icon(
                    painter = painterResource(
                        resource = iconRes?.imageRes!!
                    ),
                    contentDescription = null,
                    tint = iconRes.tint.orDefaultTint()
                )
            }
        }
    )
}

@Composable
fun defaultSwitchColors(): SwitchColors {

    return SwitchDefaults.colors(
        checkedThumbColor = ComposaTheme.color.iconNeutralinverse,
        checkedTrackColor = ComposaTheme.color.backgroundAction,
        uncheckedThumbColor = ComposaTheme.color.backgroundNeutral,
        uncheckedTrackColor = ComposaTheme.color.textNeutral.copy(alpha = 0.5f),
        disabledCheckedThumbColor = ComposaTheme.color.iconNeutralinverse.copy(alpha = 0.4f),
        disabledCheckedTrackColor = ComposaTheme.color.backgroundAction.copy(alpha = 0.4f),
        disabledUncheckedThumbColor = ComposaTheme.color.backgroundNeutral.copy(alpha = 0.4f),
        disabledUncheckedTrackColor = ComposaTheme.color.textNeutral.copy(alpha = 0.4f)
    )
}
