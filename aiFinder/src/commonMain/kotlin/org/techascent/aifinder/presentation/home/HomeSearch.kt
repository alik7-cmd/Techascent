package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun HomeSearch(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AiFinderPalette.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = AiFinderPalette.Muted,
            modifier = Modifier.size(20.dp)
        )
        Text(
            stringResource(Res.string.home_search_hint),
            color = AiFinderPalette.Muted,
            fontSize = 17.sp
        )
    }
}
