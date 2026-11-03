package org.techascent.aifinder.presentation.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.PrimaryAction
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSheet(
    state: SubscriptionUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedScans by rememberSaveable { mutableStateOf(5) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AiFinderPalette.Surface,
        contentColor = AiFinderPalette.Ink,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier.padding(vertical = 16.dp).size(64.dp, 5.dp)
                    .background(AiFinderPalette.Border, CircleShape)
            )
        }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.subscription_title),
                    color = AiFinderPalette.Ink,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(Res.string.subscription_close),
                        tint = AiFinderPalette.Muted
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (state) {
                    SubscriptionUiState.Loading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = AiFinderPalette.Teal, strokeWidth = 2.dp)
                        Text(stringResource(Res.string.subscription_checking), color = AiFinderPalette.Muted)
                    }
                    SubscriptionUiState.Error -> Column {
                        Text(stringResource(Res.string.subscription_access_error), color = AiFinderPalette.Ink)
                        TextButton(onClick = onRetry) {
                            Text(stringResource(Res.string.location_retry), color = AiFinderPalette.Teal)
                        }
                    }
                    is SubscriptionUiState.Ready -> Text(
                        stringResource(
                            if (state.access.isMaster) Res.string.subscription_master_access
                            else Res.string.subscription_scans_remaining,
                            state.access.remainingScans
                        ),
                        color = AiFinderPalette.Teal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    stringResource(Res.string.subscription_choose_pack),
                    color = AiFinderPalette.Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScanPackCard(
                        scans = 3,
                        price = Res.string.subscription_small_price,
                        selected = selectedScans == 3,
                        bestValue = false,
                        onSelect = { selectedScans = 3 }
                    )
                    ScanPackCard(
                        scans = 5,
                        price = Res.string.subscription_large_price,
                        selected = selectedScans == 5,
                        bestValue = true,
                        onSelect = { selectedScans = 5 }
                    )
                }
                PurchaseActions()
            }
        }
    }
}

@Composable
private fun PurchaseActions() {
    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PrimaryAction(
            label = stringResource(Res.string.subscription_purchase_unavailable),
            onClick = {},
            enabled = false,
            modifier = Modifier.heightIn(min = 54.dp)
        )
        Text(
            stringResource(Res.string.subscription_preview_notice),
            color = AiFinderPalette.Muted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScanPackCard(
    scans: Int,
    price: StringResource,
    selected: Boolean,
    bestValue: Boolean,
    onSelect: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (selected) AiFinderPalette.Mint else AiFinderPalette.Background)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(Res.string.subscription_pack_scans, scans),
                    color = AiFinderPalette.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (bestValue) {
                    Text(
                        stringResource(Res.string.subscription_best_value),
                        color = AiFinderPalette.Teal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End
                    )
                }
            }
            Text(
                stringResource(price),
                color = if (selected) AiFinderPalette.Teal else AiFinderPalette.Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = AiFinderPalette.Teal,
                unselectedColor = AiFinderPalette.Muted
            )
        )
    }
}
