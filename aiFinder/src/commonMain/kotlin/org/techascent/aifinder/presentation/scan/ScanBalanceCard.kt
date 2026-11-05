package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        shape = RoundedCornerShape(28.dp),
        color = AiFinderPalette.Teal,
    ) {
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF174A48), AiFinderPalette.Teal))
            )
        ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.04f), size.width * 0.5f, Offset(size.width, 0f))
            drawCircle(Color.White.copy(alpha = 0.05f), size.width * 0.3f, Offset(size.width, 0f))
        }
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome, null,
                    tint = AiFinderPalette.IllustrationMint,
                    modifier = Modifier.size(20.dp),
                )
                    Text(
                        stringResource(Res.string.scan_balance_title),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    )
            }
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    when {
                        data.isLoadingBalance -> CircularProgressIndicator(
                            Modifier.padding(top = 8.dp).size(20.dp),
                            color = AiFinderPalette.IllustrationMint,
                            strokeWidth = 2.dp,
                        )
                        data.balanceError -> Text(
                            stringResource(Res.string.scan_balance_error),
                            color = Color.White,
                            fontSize = 14.sp,
                        )
                        else -> Text(
                            data.access?.remainingScans?.toString() ?: stringResource(Res.string.scan_balance_error),
                            color = Color.White,
                            fontSize = if (data.access == null) 24.sp else 44.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (!data.isLoadingBalance && !data.balanceError && data.access != null) {
                        Text(
                            stringResource(Res.string.credit_preview_scans_available),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                        )
                    }
                }
                if (data.balanceError) {
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(contentColor = AiFinderPalette.IllustrationMint),
                    ) { Text(stringResource(Res.string.scan_balance_retry)) }
                }
                Text(
                    stringResource(if (empty) Res.string.credit_preview_add_credits else Res.string.credit_preview_scan_cost),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            if (empty) {
                if (data.purchasePreparationError) {
                    Text(
                        stringResource(Res.string.subscription_access_error),
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (data.isPreparingPurchase) {
                    Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AiFinderPalette.IllustrationMint,
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    TextButton(
                        onClick = onGetScans,
                        enabled = !data.isAnalyzing,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AiFinderPalette.IllustrationMint,
                            disabledContentColor = Color.White.copy(alpha = 0.5f),
                        ),
                    ) {
                        Text(stringResource(Res.string.scan_get_scans))
                    }
                    }
                }
            }
            }
        }
    }
}
