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
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
fun SubscriptionSheet(
    state: SubscriptionUiState,
    onRetry: () -> Unit,
    onPurchaseCompleted: () -> Unit,
    onDismiss: () -> Unit,
) {
    RevenueCatPaywall(onPurchaseCompleted = onPurchaseCompleted, onDismiss = onDismiss)
}

@Composable
private fun RevenueCatPaywall(onPurchaseCompleted: () -> Unit, onDismiss: () -> Unit) {
    val latestOnPurchaseCompleted by rememberUpdatedState(onPurchaseCompleted)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val options = remember {
        var purchased = false
        PaywallOptions(dismissRequest = { if (!purchased) latestOnDismiss() }) {
            shouldDisplayDismissButton = true
            listener = object : PaywallListener {
                override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                    purchased = true
                    latestOnPurchaseCompleted()
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
