package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import apphub.aifinder.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun DetectionCard(detection: ItemDetection, onEdit: () -> Unit, onDelete: () -> Unit, enabled: Boolean = true) {
    val confidence = detection.confidence
    val categoryIcon = when (detection.item.category) {
        "Electronics" -> Icons.Outlined.Cable
        "Documents" -> Icons.Outlined.Description
        "Accessories" -> Icons.Outlined.VpnKey
        else -> Icons.Outlined.Category
    }
    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(AiFinderPalette.Mint, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(categoryIcon, contentDescription = null, tint = AiFinderPalette.Teal)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        detection.item.name,
                        color = AiFinderPalette.Ink,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(detection.item.category, color = AiFinderPalette.Muted, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (confidence != null) {
                    Text(
                        stringResource(
                            Res.string.review_confidence_label,
                            "${(confidence.coerceIn(0f, 1f) * 100).roundToInt()}%",
                        ),
                        color = AiFinderPalette.Muted,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, enabled = enabled) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = stringResource(Res.string.review_edit_item, detection.item.name),
                        tint = AiFinderPalette.Teal
                    )
                }
                IconButton(onClick = onDelete, enabled = enabled) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(Res.string.review_delete_item, detection.item.name),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
