package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun HomeHeader() {
    Column(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle(stringResource(Res.string.home_title))
        Text(
            stringResource(Res.string.home_greeting),
            color = AiFinderPalette.Muted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
