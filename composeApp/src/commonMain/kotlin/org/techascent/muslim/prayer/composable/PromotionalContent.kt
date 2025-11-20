package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.ic_halal
import apphub.composeapp.generated.resources.ic_scan
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_halal_promotion
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.title_halal_scanner
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.DrawableData
import org.techascent.composa.featurecard.FeatureCard
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog

internal fun LazyListScope.featureCard(
    onClick: () -> Unit,
) {
    item {
        val coroutineScope = rememberCoroutineScope()
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }
        BindEffect(controller)
        val title = stringResource(Res.string.text_permission_title)
        val message = stringResource(Res.string.text_permission_description)
        val confirmText = stringResource(Res.string.button_open_settings)
        val cancelText = stringResource(Res.string.text_cancel)
        FeatureCard(
            icon = DrawableData(
                imageRes = Res.drawable.ic_halal,
                tint = ComposaTheme.color.iconAction
            ),
            text = stringResource(Res.string.text_halal_promotion),
            buttonText = stringResource(Res.string.title_halal_scanner),
            leftIcon = DrawableData(
                imageRes = Res.drawable.ic_scan,
                tint = ComposaTheme.color.iconAction
            ),
            onClick = {
                coroutineScope.launch {
                    try {
                        controller.providePermission(Permission.CAMERA)
                        onClick()
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
            }
        )
    }

}