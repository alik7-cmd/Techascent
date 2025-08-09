package org.techascent.muslim.prayer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_suhur
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.prayer.uimodel.IftarTimeUiModel

@Composable
internal fun IftarTimeContent(
    modifier: Modifier = Modifier,
    iftarTime: IftarTimeUiModel?
) {
    ComposaCardFrame(
        modifier = modifier.testTag(PrayerTags.PRAYER_TIME_IFTAR_TIME_CONTENT),
        borderColor = ComposaTheme.color.strokeNeutralSubtle,
        content = {
            Cell(
                leftSlot = LeftSlot.None,
                centerSlot = CenterSlot.Title(
                    title = stringResource(resource = Res.string.text_iftar)
                ),
                rightSlot = RightSlot.TextEmphasized(
                    text = iftarTime?.iftarStartTime ?: "-"
                )
            )

            Cell(
                leftSlot = LeftSlot.None,
                centerSlot = CenterSlot.Title(
                    title = stringResource(resource = Res.string.text_suhur)
                ),
                rightSlot = RightSlot.TextEmphasized(
                    text = iftarTime?.lastTimeOfSahri ?: "-"
                )
            )

        }

    )
}