package org.techascent.muslim.common

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.computer_error
import org.techascent.composa.button.text.ComposaButtonText
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme

@Composable
fun IconWithText(
    modifier: Modifier = Modifier,
    text: String,
    drawableData: DrawableData?,
    textStyle: TextStyle,
    textColor: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
    ) {
        drawableData?.let {
            ComposaIcon(
                modifier = Modifier
                    .size(ComposaSpacing.Medium),
                icon = it,
            )
        }

        Text(
            text = text,
            style = textStyle,
            color = textColor
        )
    }
}

@Composable
fun ErrorPoster(
    title: String?,
    description: String,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ComposaSpacing.Medium),
        verticalArrangement = spacedBy(ComposaSpacing.Small)
    ) {

        ComposaIcon(
            modifier = Modifier
                .size(200.dp)
                .align(CenterHorizontally),
            icon = DrawableData(
                imageRes = Res.drawable.computer_error,
                tint = ComposaTheme.color.textNeutral
            ),
        )

        ComposaButtonText(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.align(CenterHorizontally),
        )

        title?.let {
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = it,
                textAlign = TextAlign.Center,
                style = ComposaTheme.typography.titleEmphasized,
                color = ComposaTheme.color.textNeutral
            )
        }

        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = description,
            textAlign = TextAlign.Center,
            style = ComposaTheme.typography.body,
            color = ComposaTheme.color.textNeutral
        )


    }
}

fun getImageAspectRatioForWindowSize(widthSizeClass: WindowWidthSizeClass): Float {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16f / 9f
        WindowWidthSizeClass.Medium -> 4f / 3f
        WindowWidthSizeClass.Expanded -> 3f / 2f
        else -> 16f / 9f
    }
}


