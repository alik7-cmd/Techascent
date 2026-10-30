package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.StorageLocation
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun SpaceCard(location: StorageLocation, onClick: () -> Unit) {
    val icon = when (location.id) {
        "living" -> Icons.Outlined.Widgets
        "bed" -> Icons.Outlined.ViewAgenda
        else -> Icons.Outlined.GridOn
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AiFinderPalette.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.size(60.dp)
                .background(AiFinderPalette.Mint, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AiFinderPalette.Teal, modifier = Modifier.size(26.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(location.name, color = AiFinderPalette.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(
                if (location.boxCount != null) {
                    stringResource(Res.string.home_box_summary, location.boxCount, location.itemCount)
                } else {
                    stringResource(Res.string.home_space_summary, location.spaceCount, location.itemCount)
                },
                color = AiFinderPalette.Muted,
                fontSize = 14.sp
            )
        }
    }
}
