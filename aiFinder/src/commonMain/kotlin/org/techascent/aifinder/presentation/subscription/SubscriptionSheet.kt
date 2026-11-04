package org.techascent.aifinder.presentation.subscription

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
    RevenueCatPaywall(onPurchaseCompleted = onPurchaseCompleted, onDismiss = onDismiss)
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
