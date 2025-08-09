package org.techascent.composa.button.primary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.button.ButtonContent
import org.techascent.composa.button.ComposaButtonDefaults
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.buttonDefaultMinSize
import org.techascent.composa.common.contentPadding


@Composable
internal fun ComposaButtonInternal(
    onClick: () -> Unit,
    isSmall: Boolean,
    text: String,
    isEnabled: Boolean,
    buttonState: ButtonState,
    leftIcon: DrawableData?,
    rightIcon: DrawableData?,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
    iconTint: Color,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .buttonDefaultMinSize(isSmall)
            .widthIn(max = 300.dp)
            .semantics { contentDescription = text },
        enabled = isEnabled,
        colors = colors,
        contentPadding = contentPadding(buttonState, isSmall),
        elevation = ComposaButtonDefaults.buttonElevation(),
        shape = CircleShape,
        content = {
            ComposaButtonAnimatedContent(buttonState) { targetExpanded ->
                if (targetExpanded) {
                    ButtonContent(
                        isSmall = isSmall,
                        isEmphasized = false,
                        text = text,
                        iconLeft = leftIcon,
                        iconRight = rightIcon
                    )
                } else {
                    TransformButton(
                        buttonState = buttonState,
                        color = iconTint
                    )
                }
            }
        }
    )
}

@Composable
internal fun ComposaButtonAnimatedContent(
    buttonState: ButtonState,
    content: @Composable (targetExpanded: Boolean) -> Unit,
) {
    AnimatedContent(
        targetState = buttonState == ButtonState.Idle,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    150,
                    150,
                )
            ) togetherWith scaleOut(animationSpec = tween(150))
        },
    ) { targetExpanded ->
        content(targetExpanded)
    }
}

@Composable
internal fun TransformButton(
    buttonState: ButtonState,
    color: Color
) {
    when (buttonState) {
        is ButtonState.Loading -> {
            ComposaButtonLoading(color = color)
        }

        is ButtonState.Failed,
        is ButtonState.Success -> {
            buttonState.iconData?.apply {
                ComposaButtonAnimatedIcon(
                    visibleState = true,
                    tint = color,
                    iconRes = imageRes,
                    iconContentDescription = contentDescription,
                )
            }
        }

        else -> {
            // NO-OP
        }
    }
}

@Composable
internal fun ComposaButtonLoading(color: Color) {
    CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        color = color,
        strokeWidth = 2.dp,
    )
}

@Composable
internal fun ComposaButtonAnimatedIcon(
    visibleState: Boolean,
    iconRes: DrawableResource,
    tint: Color,
    iconContentDescription: String?,
) {
    AnimatedVisibility(
        enter = fadeIn(),
        exit = fadeOut(),
        visible = visibleState,
    ) {
        Icon(
            painter = painterResource(resource = iconRes),
            contentDescription = iconContentDescription,
            tint = tint,
        )
    }
}
