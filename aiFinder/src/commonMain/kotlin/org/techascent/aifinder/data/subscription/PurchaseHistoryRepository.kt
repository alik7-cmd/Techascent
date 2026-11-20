package org.techascent.aifinder.data.subscription

import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase

interface PurchaseHistoryRemote {
    suspend fun load(userId: String): List<ScanPurchase>
}

internal expect fun createPurchaseHistoryRemote(): PurchaseHistoryRemote
internal expect fun formatPurchaseDate(timestamp: Long): String

class PurchaseHistoryRepository(
    private val auth: AuthRepository,
    private val remote: PurchaseHistoryRemote,
) {
    suspend fun load(): List<ScanPurchase> = withTimeoutOrNull(15_000L) {
        val userId = checkNotNull(auth.currentUser) { "Sign in to view your purchases." }.id
        val purchases = remote.load(userId)
        check(auth.currentUser?.id == userId) { "The signed-in user changed. Please try again." }
        purchases.sortedWith(compareByDescending<ScanPurchase> { it.purchasedAt }.thenBy { it.purchaseId })
    } ?: error("Loading purchase history timed out.")
}
