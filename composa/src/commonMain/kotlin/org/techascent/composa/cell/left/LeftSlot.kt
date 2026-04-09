package org.techascent.composa.cell.left

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.BackgroundShape
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.orDefaultTint
import org.techascent.composa.theming.ComposaTheme

sealed interface LeftSlot {
    data object None : LeftSlot
    data class Icon(
        val icon: DrawableData,
        val background: BackgroundShape = BackgroundShape.None
    ) : LeftSlot

    data class Emoji(
        val text: String,
    ) : LeftSlot

    data class Space(val size: Dp = ComposaSpacing.Large) : LeftSlot
}

@Composable
internal fun RowScope.LeftSlot(
    leftSlot: LeftSlot,
    modifier: Modifier = Modifier
) {
    when (leftSlot) {
        is LeftSlot.Icon -> LeftIcon(
            leftSlot = leftSlot,
            modifier = modifier
        )

        is LeftSlot.Space -> Space(modifier = modifier)
        is LeftSlot.Emoji -> Text(
            modifier = modifier,
            text = leftSlot.text,
            style = ComposaTheme.typography.body,
            color = ComposaTheme.color.textNeutral
        )

        is LeftSlot.None -> {}
    }
}

@Composable
private fun Space(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(ComposaSpacing.Large))
}

@Composable
private fun LeftIcon(
    leftSlot: LeftSlot.Icon,
    modifier: Modifier = Modifier
) {
    when (leftSlot.background) {
        BackgroundShape.None -> {
            Icon(
                modifier = modifier
                    .size(ComposaSpacing.ExtraLarge),
                painter = painterResource(resource = leftSlot.icon.imageRes),
                tint = leftSlot.icon.tint.orDefaultTint(),
                contentDescription = leftSlot.icon.contentDescription,
            )
        }

        is BackgroundShape.Circle -> {
            Box(
                modifier = modifier
                    .size(
                        if (leftSlot.background.isLarge) {
                            ComposaSpacing.ExtraLarge
                        } else {
                            ComposaSpacing.Large
                        }
                    )
                    .background(leftSlot.background.color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier
                        .size(
                            if (leftSlot.background.isLarge) {
                                ComposaSpacing.Large
                            } else {
                                ComposaSpacing.Medium
                            }
                        )
                        .padding(2.dp),
                    painter = painterResource(resource = leftSlot.icon.imageRes),
                    tint = leftSlot.icon.tint.orDefaultTint(),
                    contentDescription = leftSlot.icon.contentDescription,
                )
            }
        }
    }
}
