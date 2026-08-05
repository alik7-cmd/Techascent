package org.techascent.composa.featurecard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

/**
 * A compact (grid-style) feature card with an emoji circle, title,
 * and optional description. Has a bouncy press animation.
 *
 * @param emoji The emoji to display inside the leading circle.
 * @param title The primary title text.
 * @param description Optional secondary description text.
 * @param accentColor The accent color used for background tints.
 * @param modifier Modifier for the root layout.
 * @param onClick Callback invoked when the card is tapped.
 */
@Composable
fun FeatureCardCompact(
    emoji: String,
    title: String,
    description: String? = null,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isFromQuickAccess: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compactScale",
    )

    val horizontalAlignment = if (isFromQuickAccess) Alignment.CenterHorizontally else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.06f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = horizontalAlignment,
    ) {
        // Emoji circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(ComposaSpacing.Small))

        Text(
            text = title,
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutral,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

