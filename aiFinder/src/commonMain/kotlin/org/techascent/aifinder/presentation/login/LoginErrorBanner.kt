package org.techascent.aifinder.presentation.login

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
import androidx.compose.ui.graphics.Color
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

private val ErrorContainer = Color(0xFFFDECEA)
private val ErrorBorder = Color(0xFFF6C9C4)
private val ErrorAccent = Color(0xFFC62828)
private val ErrorText = Color(0xFF5F1A16)

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
            color = ErrorContainer,
            border = BorderStroke(1.dp, ErrorBorder)
        ) {
            Row(
                Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier.size(36.dp).background(ErrorAccent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(top = 1.dp)) {
                    Text(
                        stringResource(Res.string.login_error_title),
                        color = ErrorText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(message),
                        color = ErrorText.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.login_error_dismiss),
                        tint = ErrorText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
