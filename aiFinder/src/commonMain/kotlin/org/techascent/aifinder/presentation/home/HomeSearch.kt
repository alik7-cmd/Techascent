package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
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
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(
                    AiFinderPalette.Teal.copy(alpha = 0.65f),
                    AiFinderPalette.IllustrationMint,
                    AiFinderPalette.Teal.copy(alpha = 0.35f),
                )),
                RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AiFinderSpacing.dp20, vertical = AiFinderSpacing.dp20),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp12)
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = AiFinderPalette.Muted,
            modifier = Modifier.size(20.dp)
        )
        Text(
            stringResource(Res.string.home_search_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 17.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = AiFinderPalette.Teal, modifier = Modifier.size(20.dp))
    }
}
