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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_ecternal_link
import apphub.composeapp.generated.resources.ic_quibla
import apphub.composeapp.generated.resources.ic_tasbeeh
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_tasbeeh
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.button.action.ActionButton
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.event.PrayerTimeEvent
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import kotlin.ranges.coerceAtLeast
import kotlin.time.Duration

fun LazyListScope.successContent(
    uiModel: PrayerTimeUiModel,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onHandleEvent: (PrayerTimeEvent) -> Unit
) {
    currentSalatContent(uiModel = uiModel)
    actionButtonRow(
        onNavigateToTasbeeh = onNavigateToTasbeeh,
        onNavigateToCompass = onNavigateToCompass
    )
    infoBox(
        message = uiModel.warningMessage,
        url = uiModel.apiUrl,
        onHandleEvent = onHandleEvent
    )
    salatTimeContent(uiModel = uiModel)
    spacer()
}

private fun LazyListScope.actionButtonRow(
    onNavigateToTasbeeh: () -> Unit, onNavigateToCompass: () -> Unit
) {
    item {
        LazyRow(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            horizontalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            item {
                ActionButton(
                    icon = DrawableData(
                        imageRes = Res.drawable.ic_tasbeeh,
                        tint = ComposaTheme.color.iconNeutralinverse
                    ),
                    actionText = stringResource(resource = Res.string.title_tasbeeh),
                    onClick = onNavigateToTasbeeh
                )
            }

            item {
                ActionButton(
                    icon = DrawableData(
                        imageRes = Res.drawable.ic_quibla,
                        tint = ComposaTheme.color.iconNeutralinverse
                    ),
                    actionText = stringResource(resource = Res.string.title_quibla),
                    onClick = onNavigateToCompass
                )
            }
        }
    }
}

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
                        currentPrayer = uiModel.currentPrayer.name
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
                        if (it.name != Res.string.text_salat_ud_duha) {
                            val backgroundColor =
                                if (it.displayableStartTime == uiModel.currentPrayer?.displayableStartTime) {
                                    ComposaTheme.color.backgroundInfoSubtle
                                } else {
                                    ComposaTheme.color.backgroundNeutral
                                }
                            SalatTimeCell(
                                salatName = stringResource(resource = it.name),
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
    message: StringResource, url: String, onHandleEvent: (PrayerTimeEvent) -> Unit
) {
    item {
        MessageBox(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium)
                .testTag(PrayerTags.PRAYER_TIME_INFO_CONTENT),
            messageType = MessageType.Info,
            message = stringResource(resource = message),
            navIcon = DrawableData(
                imageRes = Res.drawable.ic_ecternal_link,
                contentDescription = stringResource(resource = message),
                tint = ComposaTheme.color.textNeutral
            ),
            onClick = { onHandleEvent(PrayerTimeEvent.OpenExternalLink(url = url)) })
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
