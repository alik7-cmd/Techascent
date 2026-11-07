package org.techascent.aifinder.data.subscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.techascent.aifinder.domain.subscription.ScanPurchase
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface IosPurchaseHistoryBridge {
    fun loadPurchases(userId: String, completion: (List<ScanPurchase>?, String?) -> Unit)
}

object IosPurchaseHistoryBridgeRegistry {
    private var bridge: IosPurchaseHistoryBridge? = null
    fun install(bridge: IosPurchaseHistoryBridge) { this.bridge = bridge }
    internal fun requireBridge(): IosPurchaseHistoryBridge = checkNotNull(bridge) {
        "Install the iOS purchase history bridge before starting the app."
    }
}

internal actual fun createPurchaseHistoryRemote(): PurchaseHistoryRemote = object : PurchaseHistoryRemote {
    override suspend fun load(userId: String): List<ScanPurchase> = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            IosPurchaseHistoryBridgeRegistry.requireBridge().loadPurchases(userId) { purchases, error ->
                if (continuation.isActive) {
                    when {
                        error != null -> continuation.resumeWithException(IllegalStateException(error))
                        purchases != null -> continuation.resume(purchases)
                        else -> continuation.resumeWithException(IllegalStateException("Missing purchase history response."))
                    }
                }
            }
        }
    }
}

internal actual fun formatPurchaseDate(timestamp: Long): String = NSDateFormatter().apply {
    dateStyle = NSDateFormatterMediumStyle
    timeStyle = NSDateFormatterShortStyle
}.stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0))
