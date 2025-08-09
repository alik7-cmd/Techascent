package org.techascent.composa.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.DrawableData

@Composable
fun ComposaIcon(
    modifier: Modifier = Modifier,
    icon: DrawableData,
) {
    Icon(
        painter = painterResource(resource = icon.imageRes),
        tint = icon.tint,
        contentDescription = icon.contentDescription,
        modifier = modifier
    )
}
