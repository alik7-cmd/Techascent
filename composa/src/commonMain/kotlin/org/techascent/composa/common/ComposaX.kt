package org.techascent.composa.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.techascent.composa.button.primary.ButtonState

internal fun Modifier.clickModifier(
    onClickLabel: String?,
    onClick: (() -> Unit)?,
): Modifier {
    return if (onClick != null) {
        clickable(
            role = Role.Button,
            onClickLabel = onClickLabel,
            enabled = true,
        ) {
            onClick()
        }
    } else {
        this
    }
}

@Composable
internal fun Color?.orDefaultTint(): Color {
    if (this == null) return Color.Unspecified
    return this
}

fun Modifier.buttonDefaultMinSize(isSmall: Boolean) = defaultMinSize(
    minWidth = if (isSmall) 32.dp else 48.dp,
    minHeight = if (isSmall) 32.dp else 48.dp
)

@Composable
fun contentPadding(
    buttonState: ButtonState,
    isSmall: Boolean
) = if (buttonState == ButtonState.Idle) {
    if (isSmall) {
        PaddingValues(
            start = ComposaSpacing.Medium,
            top = ComposaSpacing.ExtraSmall,
            end = ComposaSpacing.Medium,
            bottom = ComposaSpacing.ExtraSmall
        )
    } else {
        PaddingValues(
            start = ComposaSpacing.Large,
            top = 0.dp,
            end = ComposaSpacing.Large,
            bottom = 0.dp
        )
    }
} else {
    PaddingValues()
}
