package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun SpaceCard(location: StorageLocation, onClick: () -> Unit, parentPath: String? = null, coverPhotoId: String? = null) {
    val icon = when (location.type) {
        LocationType.HOME, LocationType.AREA -> Icons.Outlined.GridOn
        LocationType.ROOM -> Icons.Outlined.Widgets
        LocationType.CABINET, LocationType.SHELF, LocationType.DRAWER -> Icons.Outlined.ViewAgenda
        LocationType.CONTAINER, LocationType.BOX -> Icons.Outlined.Inventory2
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AiFinderPalette.Surface)
            .clickable(onClick = onClick)
            .padding(AiFinderSpacing.dp16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp20)
    ) {
        org.techascent.aifinder.presentation.components.ItemPhoto(
            coverPhotoId, stringResource(Res.string.place_scan_photo, location.name), RoundedCornerShape(16.dp),
            modifier = Modifier.size(60.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AiFinderPalette.Teal, modifier = Modifier.size(26.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp6)) {
            parentPath?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
            Text(location.name, color = AiFinderPalette.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(
                if (location.boxCount != null) {
                    stringResource(Res.string.home_box_summary, location.boxCount, location.itemCount)
                } else {
                    stringResource(Res.string.home_space_summary, location.spaceCount, location.itemCount)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(20.dp))
    }
}
