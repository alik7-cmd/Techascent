package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
internal fun ScanBalanceCardV3(
    data: ScanContent,
    onRetry: () -> Unit,
    onGetScans: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = AiFinderPalette.IllustrationMint
    val hasBalance = !data.isLoadingBalance && !data.balanceError && data.access != null
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(24.dp),
        color = AiFinderPalette.Teal,
    ) {
        Box(
            Modifier.background(Brush.linearGradient(listOf(Color(0xFF174A48), AiFinderPalette.Teal)))
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(Color.White.copy(alpha = 0.04f), size.width * 0.45f, Offset(size.width, 0f))
                drawCircle(Color.White.copy(alpha = 0.05f), size.width * 0.28f, Offset(size.width, 0f))
            }
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = accent, modifier = Modifier.size(18.dp))
                    Text(
                        stringResource(Res.string.scan_balance_title),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        when {
                            data.isLoadingBalance -> CircularProgressIndicator(
                                Modifier.size(24.dp), color = accent, strokeWidth = 2.dp,
                            )
                            !hasBalance -> Text(
                                stringResource(Res.string.scan_balance_error),
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                            else -> Text(
                                stringResource(Res.string.subscription_scans_remaining, checkNotNull(data.access).remainingScans),
                                color = Color.White,
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (hasBalance && data.access?.canScan == false) {
                            Text(
                                stringResource(Res.string.credit_preview_add_credits),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                    when {
                        data.isPreparingPurchase -> Box(
                            Modifier.size(48.dp), contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = accent, strokeWidth = 2.dp)
                        }
                        !data.isLoadingBalance && !hasBalance -> TextButton(
                            onClick = onRetry,
                            colors = ButtonDefaults.textButtonColors(contentColor = accent),
                        ) { Text(stringResource(Res.string.scan_balance_retry)) }
                        hasBalance -> TextButton(
                            onClick = onGetScans,
                            enabled = !data.isAnalyzing,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.White.copy(alpha = 0.12f),
                                contentColor = accent,
                                disabledContentColor = Color.White.copy(alpha = 0.5f),
                            ),
                        ) {
                            Text(
                                stringResource(Res.string.scan_balance_get_more_compact),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                if (data.purchasePreparationError) {
                    Text(
                        stringResource(Res.string.subscription_access_error),
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}
