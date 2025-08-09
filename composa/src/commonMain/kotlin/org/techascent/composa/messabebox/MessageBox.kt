package org.techascent.composa.messabebox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import apphub.composa.generated.resources.Res
import apphub.composa.generated.resources.baseline_error
import apphub.composa.generated.resources.baseline_info
import apphub.composa.generated.resources.baseline_warning
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.common.clickModifier
import org.techascent.composa.common.orDefaultTint
import org.techascent.composa.theming.ComposaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBox(
    modifier: Modifier,
    message: String,
    messageType: MessageType = MessageType.Info,
    navIcon: DrawableData? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null
) {
    val backgroundColor = getMessageBoxBackgroundColor(messageType)
    val leftIcon = getMessageBoxIcon(messageType)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickModifier(onClick = onClick, onClickLabel = onClickLabel)
            .background(
                color = backgroundColor,
                shape = ComposaTheme.shape.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(
                    start = ComposaSpacing.Medium,
                    end = ComposaSpacing.Small,
                    top = ComposaSpacing.Small,
                    bottom = ComposaSpacing.Small,
                )
                .align(Alignment.CenterVertically),
            painter = painterResource(resource = leftIcon.imageRes),
            contentDescription = leftIcon.contentDescription,
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(8f)
                .padding(8.dp)
                .padding(end = 16.dp),
            text = message,
            style = ComposaTheme.typography.footnote,

            color = ComposaTheme.color.textNeutral,
            textAlign = TextAlign.Start
        )

        navIcon?.let {
            Icon(
                modifier = modifier
                    .align(Alignment.CenterVertically)
                    .weight(2f),
                painter = painterResource(resource = it.imageRes),
                tint = it.tint.orDefaultTint(),
                contentDescription = it.contentDescription
            )
        }
    }
}

@Composable
private fun getMessageBoxIcon(messageType: MessageType): DrawableData {
    return when (messageType) {
        is MessageType.Error -> DrawableData(
            imageRes = Res.drawable.baseline_error,
            contentDescription = "Error",
            tint = Color.Unspecified
        )

        is MessageType.Info -> DrawableData(
            imageRes = Res.drawable.baseline_info,
            contentDescription = "Info",
            tint = Color.Unspecified
        )

        is MessageType.Warning -> DrawableData(
            imageRes = Res.drawable.baseline_warning,
            contentDescription = "Warning",
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun getMessageBoxBackgroundColor(messageType: MessageType): Color {
    return when (messageType) {
        is MessageType.Error -> ComposaTheme.color.backgroundErrorSubtle
        is MessageType.Info -> ComposaTheme.color.backgroundInfoSubtle
        is MessageType.Warning -> ComposaTheme.color.backgroundWarningSubtle
    }
}
