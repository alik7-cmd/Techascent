package org.techascent.muslim.calendar.composable

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_salat_ud_duha
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.composable.IftarTimeContent
import org.techascent.muslim.prayer.composable.SalatTimeCell
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

fun LazyListScope.successContent(
    uiModel: PrayerTimeUiModel,
) {
    item {
        SalatContent(
            uiModel = uiModel
        )
    }
}

@Composable
private fun SalatContent(
    uiModel: PrayerTimeUiModel
) {

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(horizontal = ComposaSpacing.Medium),
        horizontalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
    ) {
        IftarTimeContent(
            modifier = Modifier
                .weight(0.5f),
            iftarTime = uiModel.iftarTime
        )
        ComposaCardFrame(
            modifier = Modifier
                .fillMaxHeight().weight(0.5f),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                uiModel.intervals.forEach {
                    if (it.name != Res.string.text_salat_ud_duha) {
                        SalatTimeCell(
                            salatName = stringResource(resource = it.name),
                            salatTime = "${it.displayableStartTime} - ${it.displayableEndTime}",
                            backgroundColor = Color.Unspecified
                        )
                    }
                }
            }
        )
    }

}