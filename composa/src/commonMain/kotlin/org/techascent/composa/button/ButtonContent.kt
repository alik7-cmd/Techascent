package org.techascent.composa.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.theming.ComposaTheme

@Composable
@Suppress("ComplexMethod")
internal fun ButtonContent(
    isSmall: Boolean,
    isEmphasized: Boolean,
    text: String,
    iconLeft: DrawableData?,
    iconRight: DrawableData?,
    textAlign: TextAlign = TextAlign.Center,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val typography = when {
            isSmall && !isEmphasized -> ComposaTheme.typography.buttonSmall
            isSmall && isEmphasized -> ComposaTheme.typography.buttonSmallEmphasized
            !isSmall && !isEmphasized -> ComposaTheme.typography.buttonLarge
            else -> ComposaTheme.typography.buttonLargeEmphasized
        }

        ProvideTextStyle(value = typography) {
            // Left Icon
            iconLeft?.apply {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(resource = imageRes),
                    contentDescription = contentDescription,
                    tint = tint
                )
            }

            // Center text
            Box(
                Modifier
                    .weight(1f, fill = false)
                    .padding(
                        start = if (iconLeft != null) ComposaSpacing.Small else 0.dp,
                        end = if (iconRight != null) ComposaSpacing.Small else 0.dp
                    )
            ) {
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = text,
                    textAlign = textAlign,
                )
            }

            // Right icon
            iconRight?.apply {
                Icon(
                    modifier = Modifier.Companion.size(ComposaSpacing.Medium),
                    painter = painterResource(resource = imageRes),
                    contentDescription = contentDescription,
                    tint = tint
                )
            }
        }
    }
}
