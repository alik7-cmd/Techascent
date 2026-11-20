package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techascent.aifinder.presentation.components.ShimmerBlock
import org.techascent.aifinder.presentation.theme.AiFinderPalette

internal const val HOME_SKELETON_CARDS = 3

/** Skeleton of the "Your spaces" header shown while the cache is read. */
@Composable
internal fun SpacesHeaderSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = AiFinderSpacing.dp12, bottom = AiFinderSpacing.dp4),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBlock(Modifier.width(140.dp).height(22.dp))
        ShimmerBlock(Modifier.width(56.dp).height(18.dp))
    }
}

/** Mirrors [SpaceCard]'s layout so content doesn't jump when it arrives. */
@Composable
internal fun SpaceCardSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(AiFinderPalette.Surface, RoundedCornerShape(20.dp))
            .padding(AiFinderSpacing.dp16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp20),
    ) {
        ShimmerBlock(Modifier.size(60.dp), RoundedCornerShape(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp10)) {
            ShimmerBlock(Modifier.fillMaxWidth(0.55f).height(20.dp))
            ShimmerBlock(Modifier.fillMaxWidth(0.8f).height(14.dp))
        }
    }
}
