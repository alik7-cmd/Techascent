package org.techascent.composa.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.techascent.composa.theming.ComposaTheme

/**
 * A customizable card-like container with optional background color and border styling.
 *
 * `ComposaCardFrame` wraps a [Card] with a consistent rounded shape and allows applying
 * a border and an optional background (container) color.
 *
 * @param modifier The [Modifier] to be applied to the card.
 * @param content The composable content to be placed inside the card.
 * @param borderColor The color of the border around the card.
 * @param containerColor Optional background color for the card. If null, the default
 *        card background color is used.
 *
 * ### Example
 * ```
 * ComposaCardFrame(
 *         modifier = Modifier.fillMaxWidth(),
 *         borderColor = Color.Gray,
 *         containerColor = Color.White,
 *         content = {...add your composables here...}
 * ```
 */
@Composable
fun ComposaCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    borderColor: Color = Color.Unspecified,
    containerColor: Color? = ComposaTheme.color.backgroundAppBackground
) {
    val shape = ComposaTheme.shape.large
    Card(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = shape,
            )
            .clip(shape),
        shape = shape,
        colors = containerColor?.let {
            CardDefaults.cardColors(containerColor = it)
        } ?: CardDefaults.cardColors(),
    ) {
        Column {
            content()
        }
    }
}

