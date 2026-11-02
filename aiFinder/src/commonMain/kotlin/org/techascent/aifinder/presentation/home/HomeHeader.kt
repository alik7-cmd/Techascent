package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun HomeHeader(onAccount: () -> Unit) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(stringResource(Res.string.home_title))
            Text(
                stringResource(Res.string.home_greeting),
                color = AiFinderPalette.Muted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        IconButton(onClick = onAccount) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = stringResource(Res.string.account_open),
                tint = AiFinderPalette.Teal,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
