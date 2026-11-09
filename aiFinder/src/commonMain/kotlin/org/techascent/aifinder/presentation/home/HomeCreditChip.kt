package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.scan.ScanCreditDetails
import org.techascent.aifinder.presentation.scan.ScanContent
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.presentation.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeCreditChip(
    data: ScanContent,
    onRetry: () -> Unit,
    onGetScans: () -> Unit,
) {
    var showingDetails by rememberSaveable { mutableStateOf(false) }
    val balance = data.access?.takeUnless { data.balanceError }
    AssistChip(
        onClick = { showingDetails = true },
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(50),
        label = {
            Text(stringResource(when {
                data.isPreparingPurchase -> Res.string.subscription_checking
                data.isLoadingBalance -> Res.string.scan_balance_checking
                balance == null -> Res.string.home_check_credits
                balance.remainingScans == 1L -> Res.string.home_credit_one
                else -> Res.string.home_credit_count
            }, balance?.remainingScans ?: 0L))
        },
        leadingIcon = {
            if (data.isLoadingBalance || data.isPreparingPurchase) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AiFinderPalette.Teal)
            } else {
                Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(18.dp))
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = AiFinderPalette.Mint,
            labelColor = AiFinderPalette.Teal,
            leadingIconContentColor = AiFinderPalette.Teal,
        ),
    )
    if (showingDetails) {
        ModalBottomSheet(
            onDismissRequest = { showingDetails = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AiFinderPalette.Background,
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SectionTitle(stringResource(Res.string.scan_balance_title))
                Text(
                    stringResource(Res.string.credits_sheet_description),
                    color = AiFinderPalette.Muted,
                )
                ScanCreditDetails(data, onRetry, onGetScans)
                TextButton(onClick = { showingDetails = false }) {
                    Text(stringResource(Res.string.credits_close))
                }
            }
        }
    }
}
