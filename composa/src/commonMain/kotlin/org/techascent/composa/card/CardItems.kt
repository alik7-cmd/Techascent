package org.techascent.composa.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.techascent.composa.theming.ComposaTheme


@OptIn(ExperimentalFoundationApi::class)
inline fun <T> LazyListScope.cardItemsIndexed(
    items: List<T>,
    horizontalPadding: Dp = 0.dp,
    animateItemPlacement: Boolean = false,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
): Unit = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(index, items[index]) } else null,
    contentType = { index -> contentType(index, items[index]) }
) { index ->
    val onBackground = ComposaTheme.color.backgroundAppBackground
    val strokeWidth = 0.5.dp
    val cornerRadius = 8.dp
    val modifier = when {
        items.size == 1 -> {
            Modifier
                .border(
                    width = strokeWidth,
                    color = ComposaTheme.color.backgroundAppBackground,
                    shape = ComposaTheme.shape.medium,
                )
                .clip(RoundedCornerShape(cornerRadius))

        }

        index == 0 -> {
            Modifier
                .topBorder(
                    cornerRadius = cornerRadius,
                    strokeWidth = strokeWidth,
                    color = onBackground
                )
        }

        index == items.lastIndex -> {
            Modifier
                .bottomBorder(
                    cornerRadius = cornerRadius,
                    strokeWidth = strokeWidth,
                    color = onBackground
                )
        }

        else -> {
            Modifier
                .sideBorder(
                    strokeWidth = strokeWidth,
                    color = onBackground,
                )
        }
    }
    Box(
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .then(modifier.background(color = ComposaTheme.color.backgroundAppBackground))
            .then(
                if (animateItemPlacement) {
                    Modifier.animateItem()
                } else {
                    Modifier
                }
            )
    ) {
        itemContent(index, items[index])
    }
}
