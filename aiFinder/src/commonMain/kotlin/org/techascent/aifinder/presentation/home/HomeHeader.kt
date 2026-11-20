package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

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
import org.techascent.aifinder.presentation.scan.ScanContent

@Composable
internal fun HomeHeader(
    onAccount: () -> Unit,
    scanBalance: ScanContent,
    onRetryBalance: () -> Unit,
    onGetScans: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(bottom = AiFinderSpacing.dp8),
        verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp8),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HomeCreditChip(scanBalance, onRetryBalance, onGetScans)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAccount) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(Res.string.account_open),
                    tint = AiFinderPalette.Teal,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        SectionTitle(stringResource(Res.string.home_title))
        Text(
            stringResource(Res.string.home_greeting),
            color = AiFinderPalette.Muted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
