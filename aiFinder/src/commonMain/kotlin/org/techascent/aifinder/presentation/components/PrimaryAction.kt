package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.techascent.composa.button.primary.ButtonState
import org.techascent.composa.button.primary.ComposaButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val isInteractive = enabled && !isLoading
    val containerColor by animateColorAsState(
        targetValue = if (isInteractive || isLoading) AiFinderPalette.Teal else AiFinderPalette.DisabledSurface,
        label = "primaryActionContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isInteractive || isLoading) Color.White else AiFinderPalette.DisabledContent,
        label = "primaryActionContent"
    )
    ComposaButton(
        text = label,
        isEnabled = isInteractive,
        onClick = onClick,
        modifier = modifier.fillMaxWidth().then(
            if (isInteractive || isLoading) Modifier
            else Modifier.border(1.dp, AiFinderPalette.DisabledBorder, CircleShape)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        iconTint = contentColor,
        buttonState = if (isLoading) ButtonState.Loading else ButtonState.Idle
    )
}
