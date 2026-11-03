package org.techascent.aifinder.data.subscription

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import kotlinx.coroutines.withTimeout
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import kotlin.time.Duration.Companion.seconds

internal expect fun revenueCatApiKey(): String

class RevenueCatPurchaseGateway(
    private val apiKey: String,
    private val currentUserId: () -> String?,
) : PurchaseGateway {
    override fun prepare(): Boolean {
        if (apiKey.isBlank()) return false
        if (!Purchases.isConfigured) {
            Purchases.configure(PurchasesConfiguration(apiKey) { appUserId = currentUserId() })
        }
        return true
    }

    override suspend fun identify(userId: String) {
        if (!prepare()) return
        val purchases = Purchases.sharedInstance
        if (purchases.appUserID == userId) return
        withTimeout(15.seconds) { purchases.awaitLogIn(userId) }
    }

    override suspend fun reset() {
        if (apiKey.isBlank() || !Purchases.isConfigured) return
        val purchases = Purchases.sharedInstance
        if (!purchases.isAnonymous) withTimeout(15.seconds) { purchases.awaitLogOut() }
    }
}
