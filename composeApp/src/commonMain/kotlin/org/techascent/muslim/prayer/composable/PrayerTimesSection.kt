package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.ic_notification_off
import apphub.composeapp.generated.resources.ic_notification_on
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_notification_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_prayer_all_times
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.DrawableData
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.common.localizeTime
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog

@Composable
internal fun PrayerTimesSection(
    uiModel: PrayerTimeUiModel,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    BindEffect(ctrl)
    val permTitle = stringResource(Res.string.text_permission_title)
    val permMessage = stringResource(Res.string.text_notification_permission_description)
    val permOpen = stringResource(Res.string.button_open_settings)
    val permCancel = stringResource(Res.string.text_cancel)

    val currentWaqtBg = ComposaTheme.color.backgroundActionSubtle
    val currentWaqtText = ComposaTheme.color.prayer.currentWaqtText
    val cardBg = ComposaTheme.color.prayer.cardBg

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
    ) {
        /*Cell(
            leftSlot = LeftSlot.Emoji(
                emoji = "🕌",
                accentColor = currentWaqtText,
                size = 34.dp,
                fontSize = 18,
            ),
            centerSlot = CenterSlot.Title(title = stringResource(Res.string.text_prayer_all_times)),
            rightSlot = RightSlot.None,
            backgroundColor = Color.Transparent,
        )
        HorizontalDivider(
            Modifier.padding(horizontal = 12.dp),
            0.5.dp,
            ComposaTheme.color.strokeNeutralSubtle.copy(0.4f),
        )*/

        val intervals = uiModel.intervals.filter { it.name != PrayerNameEnum.SALAT_UD_DUHA }
        intervals.forEachIndexed { idx, interval ->
            val isCurrent = interval.name == uiModel.currentPrayer?.name
            var notify by remember { mutableStateOf(interval.shouldNotify) }

            PrayerRow(
                name = stringResource(interval.name.toDisplayString()),
                time = "${interval.displayableStartTime.localizeTime()} - ${interval.displayableEndTime.localizeTime()}",
                isCurrent = isCurrent,
                shouldNotify = notify,
                emoji = interval.emoji,
                currentWaqtBg = currentWaqtBg,
                currentWaqtText = currentWaqtText,
                onClick = {
                    scope.launch {
                        try {
                            ctrl.providePermission(Permission.REMOTE_NOTIFICATION)
                            notify = !notify
                            onUpdateNotification(notify, interval.name)
                        } catch (_: DeniedException) {
                            showPermissionRationalDialog(permTitle, permMessage, permOpen, permCancel, { ctrl.openAppSettings() })
                        } catch (_: DeniedAlwaysException) {
                            showPermissionRationalDialog(permTitle, permMessage, permOpen, permCancel, { ctrl.openAppSettings() })
                        }
                    }
                },
            )

            if (idx < intervals.lastIndex) {
                HorizontalDivider(
                    Modifier.padding(horizontal = 12.dp),
                    0.5.dp,
                    ComposaTheme.color.strokeNeutralSubtle.copy(0.3f),
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    name: String,
    time: String,
    isCurrent: Boolean,
    shouldNotify: Boolean,
    emoji: String,
    currentWaqtBg: Color,
    currentWaqtText: Color,
    onClick: () -> Unit,
) {
    Cell(
        leftSlot = LeftSlot.Emoji(emoji = emoji, fontSize = 16),
        centerSlot = CenterSlot.TitleWithLabel(title = name, label = time),
        rightSlot = RightSlot.Icon(
            data = DrawableData(
                imageRes = if (shouldNotify) Res.drawable.ic_notification_on else Res.drawable.ic_notification_off,
                tint = if (shouldNotify) currentWaqtText else ComposaTheme.color.textNeutralSubtle,
            ),
        ),
        backgroundColor = if (isCurrent) currentWaqtBg.copy(alpha = 0.50f) else Color.Transparent,
        onClick = onClick,
    )
}



