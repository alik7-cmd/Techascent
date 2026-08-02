package org.techascent.muslim.prayer.composable

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_utility_greeting
import apphub.composeapp.generated.resources.title_halal_scanner
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog

@Composable
internal fun GreetingRow(onNavigateHalalScanner: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .padding(top = ComposaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.text_utility_greeting),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
            modifier = Modifier.weight(1f),
        )
        HalalPill(onClick = onNavigateHalalScanner)
    }
}

// ─── Internal — only used by GreetingRow ────────────────────────────────────────

@Composable
private fun HalalPill(onClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val ctrl = remember(factory) { factory.createPermissionsController() }
    val permTitle = stringResource(Res.string.text_permission_title)
    val permMessage = stringResource(Res.string.text_permission_description)
    val permOpen = stringResource(Res.string.button_open_settings)
    val permCancel = stringResource(Res.string.text_cancel)
    BindEffect(ctrl)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pillPressScale",
    )

    val accent = ComposaTheme.color.prayer.scannerAccent
    val subtle = ComposaTheme.color.prayer.scannerSubtle

    Row(
        modifier = Modifier
            .scale(pressScale)
            .clip(RoundedCornerShape(24.dp))
            .background(subtle)
            .clickable(interactionSource = interactionSource, indication = null) {
                scope.launch {
                    try {
                        ctrl.providePermission(Permission.CAMERA)
                        onClick()
                    } catch (_: DeniedException) {
                        showPermissionRationalDialog(permTitle, permMessage, permOpen, permCancel, { ctrl.openAppSettings() })
                    } catch (_: DeniedAlwaysException) {
                        showPermissionRationalDialog(permTitle, permMessage, permOpen, permCancel, { ctrl.openAppSettings() })
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🔍", fontSize = 13.sp)
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = stringResource(Res.string.title_halal_scanner),
            style = ComposaTheme.typography.captionEmphasized,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
