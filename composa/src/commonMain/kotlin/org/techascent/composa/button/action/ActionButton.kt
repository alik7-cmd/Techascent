package org.techascent.composa.button.action

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme

/**
 * A customizable composable UI element that displays a shaped icon button with optional
 * descriptive text underneath. Commonly used for quick actions in dashboards, toolbars,
 * or interactive cards.
 *
 * The icon button is placed inside a circular (or custom-shaped) container and supports
 * click interaction. If `actionText` is provided, it is rendered below the button with
 * styling controlled by [TextDecorator].
 *
 * @param modifier Optional [Modifier] for adjusting layout behavior, size, or positioning.
 * @param icon The [DrawableData] containing the drawable resource and tint color for the icon.
 * @param backgroundColor The background color of the icon button. Default is [Color.Unspecified].
 * @param size The overall size (diameter if circular) of the icon button. Default is
 * [ComposaSpacing.ExtraExtraExtraLarge].
 * @param shape The shape of the icon button container. Defaults to [CircleShape].
 * @param actionText Optional label displayed below the icon button.
 * @param textDecorator An instance of [TextDecorator] that provides styling options for the
 * [actionText].
 * @param onClick Lambda invoked when the icon button is clicked.
 *
 * ### Example
 * ```
 * ActionButton(
 *     icon = DrawableData(imageRes = R.drawable.ic_add, tint = Color.White),
 *     actionText = "Add Item",
 *     textDecorator = TextDecorator(
 *         color = Color.Black,
 *         fontSize = 14.sp,
 *         fontWeight = FontWeight.Bold
 *     ),
 *     onClick = { /* Handle click */ }
 * )
 * ```
 */
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: DrawableData,
    size: Dp = ComposaSpacing.ExtraExtraExtraLarge,
    shape: RoundedCornerShape = CircleShape,
    actionText: String? = null,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(ComposaTheme.color.backgroundAction)
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            ComposaIcon(
                icon = icon,
            )
        }

        actionText?.let {
            Text(
                text = it,
                color = ComposaTheme.color.textNeutral,
                style = ComposaTheme.typography.footnote,
                maxLines = 1,
            )
        }
    }

}
