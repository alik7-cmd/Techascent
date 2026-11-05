package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun ScanBalanceCard(
    data: ScanContent,
    onRetry: () -> Unit,
    onGetScans: () -> Unit,
) {
    val empty = data.access?.canScan == false
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(24.dp),
        color = AiFinderPalette.Mint,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome, null,
                    tint = AiFinderPalette.Teal,
                    modifier = Modifier.size(24.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.scan_balance_title),
                        color = AiFinderPalette.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    when {
                        data.isLoadingBalance -> CircularProgressIndicator(
                            Modifier.padding(top = 8.dp).size(20.dp),
                            color = AiFinderPalette.Teal,
                            strokeWidth = 2.dp,
                        )
                        data.balanceError -> Text(
                            stringResource(Res.string.scan_balance_error),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                        )
                        else -> Text(
                            if (data.access?.isMaster == true) stringResource(Res.string.scan_balance_unlimited)
                            else stringResource(Res.string.subscription_scans_remaining, data.access?.remainingScans ?: 0L),
                            color = AiFinderPalette.Teal,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (data.balanceError) {
                    TextButton(onClick = onRetry) { Text(stringResource(Res.string.scan_balance_retry)) }
                }
            }
            if (empty) {
                Text(
                    stringResource(Res.string.scan_balance_empty),
                    color = AiFinderPalette.Ink.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                if (data.purchasePreparationError) {
                    Text(
                        stringResource(Res.string.subscription_access_error),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                    )
                }
                if (data.isPreparingPurchase) {
                    Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AiFinderPalette.Teal,
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    TextButton(onClick = onGetScans, enabled = !data.isAnalyzing) {
                        Text(stringResource(Res.string.scan_get_scans))
                    }
                }
            }
        }
    }
}
