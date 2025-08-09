package org.techascent.muslim.calendar.composable

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.shimmer.CellShimmer
import org.techascent.composa.theming.ComposaTheme

fun  LazyListScope.loadingScreen(){
    item {

        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // Ensures both children match tallest height
                .padding(horizontal = ComposaSpacing.Medium),
            horizontalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
        ) {

            ComposaCardFrame(
                modifier = Modifier.weight(0.5f),
                borderColor = ComposaTheme.color.strokeNeutralSubtle,
                content = {
                    CellShimmer()
                    CellShimmer()
                },
            )
            ComposaCardFrame(
                modifier = Modifier.weight(0.5f),
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
}