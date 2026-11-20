package org.techascent.aifinder.presentation.subscription

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.data.subscription.formatPurchaseDate
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun PurchaseHistoryScreen(viewModel: PurchaseHistoryViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }
    ScreenLayout(topBar = { ScreenTopBar(stringResource(Res.string.history_title), onBack) }) {
        item {
            Text(stringResource(Res.string.history_subtitle), color = AiFinderPalette.Muted, lineHeight = 22.sp)
        }
        when (val current = state) {
            PurchaseHistoryState.Loading -> items(4) {
                PurchaseRowSkeleton()
            }
            PurchaseHistoryState.Error -> item {
                ErrorCard(
                    stringResource(Res.string.history_error_title),
                    stringResource(Res.string.history_error_message),
                    viewModel::load,
                )
            }
            is PurchaseHistoryState.Ready -> {
                if (current.purchases.isEmpty()) item {
                    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                        Column(
                            Modifier.fillMaxWidth().padding(AiFinderSpacing.dp32),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp12),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(40.dp))
                            Text(stringResource(Res.string.history_empty_title), color = AiFinderPalette.Ink, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(Res.string.history_empty_message), color = AiFinderPalette.Muted)
                        }
                    }
                }
                items(current.purchases, key = { it.purchaseId }) { PurchaseRow(it) }
            }
        }
    }
}

@Composable
private fun PurchaseRowSkeleton() {
    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(AiFinderSpacing.dp18),
            horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp14),
            verticalAlignment = Alignment.Top,
        ) {
            ShimmerBlock(Modifier.size(44.dp), RoundedCornerShape(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp9)) {
                ShimmerBlock(Modifier.fillMaxWidth(0.55f).height(20.dp))
                ShimmerBlock(Modifier.fillMaxWidth(0.8f).height(16.dp))
                ShimmerBlock(Modifier.width(110.dp).height(26.dp))
            }
        }
    }
}

@Composable
private fun PurchaseRow(purchase: ScanPurchase) {
    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(AiFinderSpacing.dp18),
            horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp14),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                Modifier.size(44.dp).background(AiFinderPalette.Mint, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp7)) {
                Text(
                    stringResource(Res.string.history_scan_pack, purchase.scansGranted),
                    color = AiFinderPalette.Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(formatPurchaseDate(purchase.purchasedAt), color = AiFinderPalette.Muted, fontSize = 13.sp)
                Text(
                    stringResource(when (purchase.status) {
                        ScanPurchase.STATUS_CREDITED -> Res.string.history_credited
                        ScanPurchase.STATUS_PENDING -> Res.string.history_pending
                        else -> Res.string.history_status_unknown
                    }),
                    modifier = Modifier.background(AiFinderPalette.Mint, RoundedCornerShape(8.dp))
                        .padding(horizontal = AiFinderSpacing.dp10, vertical = AiFinderSpacing.dp5),
                    color = AiFinderPalette.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
