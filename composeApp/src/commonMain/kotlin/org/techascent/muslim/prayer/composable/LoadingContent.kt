package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.shimmer.CellShimmer
import org.techascent.composa.shimmer.shimmerEffect
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags

fun LazyListScope.loadingContent() {

    item {
        Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

        ComposaCardFrame(
            modifier = Modifier
                .fillMaxWidth().height(200.dp)
                .padding(horizontal = ComposaSpacing.Medium)
                .testTag(PrayerTags.PRAYER_TIME_LOADING),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                TopShimmerContent()
            },
        )

        Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

        ComposaCardFrame(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .testTag(PrayerTags.PRAYER_TIME_LOADING),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                CellShimmer()
                CellShimmer()
                CellShimmer()
                CellShimmer()
                CellShimmer()
            },
        )

    }
}

@Composable
private fun TopShimmerContent() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart) // Align to bottom-left
                .padding(ComposaSpacing.Medium),// Add your desired padding
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.ExtraSmall)
        ) {
            Text(
                text = "Text",
                modifier = Modifier
                    .size(height = 24.dp, width = 100.dp)
                    .shimmerEffect(true)
            )

            Text(
                text = "Text",
                modifier = Modifier
                    .size(height = 24.dp, width = 180.dp)
                    .shimmerEffect(true)
            )
        }
    }
}