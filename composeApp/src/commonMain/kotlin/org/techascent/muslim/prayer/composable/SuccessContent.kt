package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.warning_prayer_time
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.shared.data.enum.School
import kotlin.ranges.coerceAtLeast
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun LazyListScope.successContent(
    uiModel: PrayerTimeUiModel,
) {
    currentSalatContent(uiModel = uiModel)
    infoBox(school = uiModel.school)
    salatTimeContent(uiModel = uiModel)
    spacer()
}


@OptIn(ExperimentalTime::class)
private fun LazyListScope.salatTimeContent(
    uiModel: PrayerTimeUiModel
) {
    item {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // Ensures both children match tallest height
                .padding(horizontal = ComposaSpacing.Medium),
            horizontalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(), // Make it fill parent height
                verticalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
            ) {
                IftarTimeContent(iftarTime = uiModel.iftarTime)

                if (uiModel.currentPrayer?.startTimeInstant != null &&
                    uiModel.currentPrayer.endTimeInstant != null
                ) {
                    CountdownTimerWithProgress(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .testTag(PrayerTags.PRAYER_TIME_COUNTDOWN_TIMER_CONTENT),
                        targetTime = uiModel.currentPrayer.endTimeInstant,
                        totalDuration = (uiModel.currentPrayer.endTimeInstant - uiModel.currentPrayer.startTimeInstant)
                            .coerceAtLeast(Duration.ZERO),
                        currentPrayer = uiModel.currentPrayer.name.toDisplayString()
                    )
                }
            }

            ComposaCardFrame(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight() // Match the height of the Column
                    .testTag(PrayerTags.PRAYER_TIME_ALL_SALAT_TIME_CONTENT),
                borderColor = ComposaTheme.color.strokeNeutralSubtle,
                content = {
                    uiModel.intervals.forEach {
                        if (it.name.toDisplayString() != Res.string.text_salat_ud_duha) {
                            val backgroundColor =
                                if (it.displayableStartTime == uiModel.currentPrayer?.displayableStartTime) {
                                    ComposaTheme.color.backgroundWarningSubtle
                                } else {
                                    ComposaTheme.color.backgroundNeutral
                                }
                            SalatTimeCell(
                                salatName = stringResource(resource = it.name.toDisplayString()),
                                salatTime = "${it.displayableStartTime} - ${it.displayableEndTime}",
                                backgroundColor = backgroundColor
                            )
                        }
                    }
                }
            )
        }
    }

}

private fun LazyListScope.infoBox(
    school: School
) {

    item {
        val schoolText = stringResource(school.toTextRes())
        MessageBox(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium)
                .testTag(PrayerTags.PRAYER_TIME_INFO_CONTENT),
            messageType = MessageType.Info,
            message = stringResource(Res.string.warning_prayer_time, schoolText)
        )
    }
}

private fun LazyListScope.spacer() {
    item {
        Spacer(modifier = Modifier.size(ComposaSpacing.ExtraSmall))
    }
}

@Composable
fun SalatTimeCell(
    salatName: String, salatTime: String, backgroundColor: Color
) {
    Cell(
        leftSlot = LeftSlot.None, centerSlot = CenterSlot.TitleWithLabel(
            title = salatName, label = salatTime
        ), rightSlot = RightSlot.None, backgroundColor = backgroundColor
    )
}
