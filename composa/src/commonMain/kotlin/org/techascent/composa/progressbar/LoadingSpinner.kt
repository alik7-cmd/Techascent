package org.techascent.composa.progressbar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = ComposaSpacing.Medium,
    color: Color = ComposaTheme.color.iconAction,
    strokeWidth: Dp = 2.dp
){
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth
    )

}