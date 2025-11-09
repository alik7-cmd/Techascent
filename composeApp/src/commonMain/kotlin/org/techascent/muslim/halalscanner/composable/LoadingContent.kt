package org.techascent.muslim.halalscanner.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.shimmer.CellShimmer
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags

@Composable
internal fun LoadingContent() {
    ComposaCardFrame(
        modifier = Modifier
            .fillMaxSize()
            .testTag(PrayerTags.PRAYER_TIME_LOADING),
        borderColor = ComposaTheme.color.strokeNeutralSubtle,
        content = {
            CellShimmer()
            CellShimmer()
        },
    )
}