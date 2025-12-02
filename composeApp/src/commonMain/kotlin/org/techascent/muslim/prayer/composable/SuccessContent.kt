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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.ic_notification_off
import apphub.composeapp.generated.resources.ic_notification_on
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_notification_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.warning_prayer_time
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog
import org.techascent.shared.data.enum.School
import kotlin.ranges.coerceAtLeast
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun LazyListScope.successContent(
    uiModel: PrayerTimeUiModel,
    onNavigateHalalScanner: () -> Unit,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
) {
    currentSalatContent(uiModel = uiModel)
    infoBox(school = uiModel.school)
    salatTimeContent(
        uiModel = uiModel,
        onUpdateNotification = onUpdateNotification
    )
    featureCard(onClick = onNavigateHalalScanner)
    spacer()
}


@OptIn(ExperimentalTime::class)
private fun LazyListScope.salatTimeContent(
    uiModel: PrayerTimeUiModel,
    onUpdateNotification: (Boolean, PrayerNameEnum) -> Unit,
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

            val coroutineScope = rememberCoroutineScope()
            val factory = rememberPermissionsControllerFactory()
            val controller = remember(factory) {
                factory.createPermissionsController()
            }
            BindEffect(controller)

            val title = stringResource(Res.string.text_permission_title)
            val message = stringResource(Res.string.text_notification_permission_description)
            val confirmText = stringResource(Res.string.button_open_settings)
            val cancelText = stringResource(Res.string.text_cancel)

            ComposaCardFrame(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight() // Match the height of the Column
                    .testTag(PrayerTags.PRAYER_TIME_ALL_SALAT_TIME_CONTENT),
                borderColor = ComposaTheme.color.strokeNeutralSubtle,
                content = {
                    uiModel.intervals.forEach {
                        var shouldNotify by remember { mutableStateOf(it.shouldNotify) }
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
                                backgroundColor = backgroundColor,
                                isRightIcon = true,
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            controller.providePermission(Permission.CAMERA)
                                            shouldNotify = !shouldNotify
                                            onUpdateNotification(shouldNotify, it.name)
                                        } catch (e: DeniedException) {
                                            e.printStackTrace()
                                            showPermissionRationalDialog(
                                                title = title,
                                                message = message,
                                                confirmText = confirmText,
                                                cancelText = cancelText,
                                                onConfirm = {
                                                    controller.openAppSettings()
                                                },
                                            )
                                        } catch (e: DeniedAlwaysException) {
                                            e.printStackTrace()
                                            showPermissionRationalDialog(
                                                title = title,
                                                message = message,
                                                confirmText = confirmText,
                                                cancelText = cancelText,
                                                onConfirm = {
                                                    controller.openAppSettings()
                                                },
                                            )
                                        }
                                    }
                                    /*shouldNotify = !shouldNotify
                                    onUpdateNotification(shouldNotify, it.name)*/
                                },
                                rightDrawableData = if (shouldNotify) {
                                    DrawableData(
                                        imageRes = Res.drawable.ic_notification_on,
                                        tint = ComposaTheme.color.textNeutral
                                    )
                                } else DrawableData(
                                    imageRes = Res.drawable.ic_notification_off,
                                    tint = ComposaTheme.color.textNeutral
                                )
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
    salatName: String,
    salatTime: String,
    backgroundColor: Color,
    isRightIcon: Boolean = false,
    onClick: (() -> Unit)? = null,
    rightDrawableData: DrawableData = DrawableData(
        imageRes = Res.drawable.ic_notification_off,
        tint = ComposaTheme.color.textNeutral
    )
) {
    Cell(
        leftSlot = LeftSlot.None, centerSlot = CenterSlot.TitleWithLabel(
            title = salatName, label = salatTime
        ), rightSlot = if (isRightIcon) {
            RightSlot.Icon(
                data = rightDrawableData /*DrawableData(
                    imageRes = Res.drawable.ic_notification_off,
                    tint = ComposaTheme.color.iconAction
                )*/
            )
        } else {
            RightSlot.None
        },
        backgroundColor = backgroundColor,
        onClick = onClick
    )
}
