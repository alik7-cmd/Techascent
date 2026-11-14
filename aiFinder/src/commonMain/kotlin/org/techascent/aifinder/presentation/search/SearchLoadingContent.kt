package org.techascent.aifinder.presentation.search

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techascent.aifinder.presentation.components.ShimmerBlock
import org.techascent.aifinder.presentation.theme.AiFinderPalette

internal const val SEARCH_SKELETON_CARDS = 2

/** Skeleton for the search field, result count and result cards while the inventory loads. */
internal fun LazyListScope.searchLoadingContent() {
    item(key = "query-skeleton") {
        ShimmerBlock(Modifier.fillMaxWidth().height(56.dp), RoundedCornerShape(20.dp))
    }
    item(key = "count-skeleton") {
        ShimmerBlock(Modifier.padding(vertical = AiFinderSpacing.dp8).width(110.dp).height(16.dp))
    }
    items(SEARCH_SKELETON_CARDS, key = { "result-skeleton-$it" }) { SearchResultSkeleton() }
}

/** Mirrors a search result card so content doesn't jump when it arrives. */
@Composable
internal fun SearchResultSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(AiFinderPalette.Surface, RoundedCornerShape(24.dp))
            .padding(AiFinderSpacing.dp18),
        verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp20)
    ) {
        ShimmerBlock(Modifier.fillMaxWidth().height(180.dp), RoundedCornerShape(18.dp))
        ShimmerBlock(Modifier.fillMaxWidth(0.6f).height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBlock(Modifier.size(16.dp), CircleShape)
            ShimmerBlock(Modifier.fillMaxWidth(0.5f).height(14.dp))
        }
        ShimmerBlock(Modifier.fillMaxWidth().height(54.dp), CircleShape)
    }
}
