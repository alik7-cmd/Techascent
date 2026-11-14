package org.techascent.aifinder.presentation.login

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.login_error_dismiss
import apphub.aifinder.generated.resources.login_error_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun LoginErrorBanner(
    errorRes: StringResource?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the last message so the exit animation still has text to show.
    var lastError by remember { mutableStateOf(errorRes) }
    if (errorRes != null) lastError = errorRes

    AnimatedVisibility(
        visible = errorRes != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        val message = lastError ?: return@AnimatedVisibility
        Surface(
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Assertive },
            shape = RoundedCornerShape(20.dp),
            color = AiFinderPalette.ErrorContainer,
            border = BorderStroke(1.dp, AiFinderPalette.ErrorBorder)
        ) {
            Row(
                Modifier.padding(start = AiFinderSpacing.dp14, top = AiFinderSpacing.dp14, bottom = AiFinderSpacing.dp14, end = AiFinderSpacing.dp4),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier.size(36.dp).background(AiFinderPalette.Error.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = AiFinderPalette.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(AiFinderSpacing.dp12))
                Column(Modifier.weight(1f).padding(top = AiFinderSpacing.dp1)) {
                    Text(
                        stringResource(Res.string.login_error_title),
                        color = AiFinderPalette.ErrorText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(AiFinderSpacing.dp2))
                    Text(
                        stringResource(message),
                        color = AiFinderPalette.ErrorText.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.login_error_dismiss),
                        tint = AiFinderPalette.ErrorText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
