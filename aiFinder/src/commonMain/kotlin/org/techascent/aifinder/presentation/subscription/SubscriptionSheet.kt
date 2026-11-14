package org.techascent.aifinder.presentation.subscription

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.PrimaryAction
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import org.techascent.aifinder.domain.subscription.StorePurchase
import org.techascent.aifinder.util.Platform
import org.techascent.aifinder.util.getPlatform
import org.techascent.aifinder.util.logDebug

@Composable
fun SubscriptionSheet(
    state: SubscriptionUiState,
    onRetry: () -> Unit,
    onPurchaseCompleted: (StorePurchase) -> Unit,
    onDismiss: () -> Unit,
) {
    if (state is SubscriptionUiState.Ready && state.purchasesEnabled && !state.creditsPending) {
        RevenueCatPaywall(onPurchaseCompleted = onPurchaseCompleted, onDismiss = onDismiss)
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = AiFinderPalette.Surface) {
                Column(
                    Modifier.padding(AiFinderSpacing.dp24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16),
                ) {
                    if (state is SubscriptionUiState.Loading || state is SubscriptionUiState.AddingScans) {
                        CircularProgressIndicator(color = AiFinderPalette.Teal)
                    }
                    Text(
                        stringResource(when (state) {
                            SubscriptionUiState.Loading -> Res.string.subscription_checking
                            SubscriptionUiState.AddingScans -> Res.string.subscription_adding_scans
                            SubscriptionUiState.Error -> Res.string.subscription_access_error
                            is SubscriptionUiState.Ready -> if (state.creditsPending) Res.string.subscription_credits_pending
                                else Res.string.subscription_preview_notice
                        }),
                        color = AiFinderPalette.Ink,
                        textAlign = TextAlign.Center,
                    )
                    if (state is SubscriptionUiState.Error ||
                        (state is SubscriptionUiState.Ready && state.creditsPending)) {
                        PrimaryAction(stringResource(Res.string.scan_balance_retry), onRetry)
                    }
                    TextButton(onClick = onDismiss) { Text(stringResource(Res.string.review_cancel)) }
                }
            }
        }
    }
}

@Composable
private fun RevenueCatPaywall(
    onPurchaseCompleted: (StorePurchase) -> Unit,
    onDismiss: () -> Unit,
) {
    val latestOnPurchaseCompleted by rememberUpdatedState(onPurchaseCompleted)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val options = remember {
        var purchased = false
        PaywallOptions(dismissRequest = { if (!purchased) latestOnDismiss() }) {
            shouldDisplayDismissButton = true
            listener = object : PaywallListener {
                override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                    purchased = true
                    latestOnPurchaseCompleted(storeTransaction.toStorePurchase())
                }

                override fun onPurchaseError(error: PurchasesError) {
                    logDebug("Subscription", "Purchase failed: ${error.code} ${error.message}")
                }
            }
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize()) { Paywall(options) }
    }
}

private fun StoreTransaction.toStorePurchase() = StorePurchase(
    transactionId = transactionId,
    productIds = productIds,
    purchaseTime = purchaseTime,
    store = if (getPlatform() == Platform.IOS) "app_store" else "play_store",
)
