package org.techascent.composa.featurecard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.techascent.composa.button.text.ComposaButtonText
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme

@Composable
fun FeatureCard(
    icon: DrawableData,
    text: String,
    buttonText: String,
    leftIcon: DrawableData? = null,
    rightIcon: DrawableData? = null,
    onClick: () -> Unit,
) {
    ComposaCardFrame(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ComposaSpacing.Medium),
        borderColor = ComposaTheme.color.strokeNeutralSubtle,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(ComposaSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small)
            ) {
                ComposaIcon(
                    icon = icon,
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    text = text,
                    style = ComposaTheme.typography.buttonLarge,
                    modifier = Modifier.weight(0.8f)
                )
            }
            ComposaButtonText(
                text = buttonText,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComposaSpacing.Medium, vertical = ComposaSpacing.Small),
                leftIcon = leftIcon,
                rightIcon = rightIcon,
            )
        }
    )
}
