package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.presentation.components.InfoCard
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun DetectionCard(detection: ItemDetection) {
    val confidence = detection.confidence
    InfoCard(
        title = detection.item.name,
        subtitle = if (confidence != null) {
            stringResource(
                Res.string.review_detection_confidence,
                detection.item.category,
                (confidence * 100).roundToInt()
            )
        } else {
            stringResource(Res.string.review_detection_suggestion, detection.item.category)
        },
        titleFontSize = 18.sp,
        leadingContent = {
            Box(
                modifier = Modifier.size(60.dp)
                    .background(AiFinderPalette.Mint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Bolt, contentDescription = null, tint = AiFinderPalette.Teal)
            }
        },
        trailingContent = {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = AiFinderPalette.Teal)
        }
    )
}
