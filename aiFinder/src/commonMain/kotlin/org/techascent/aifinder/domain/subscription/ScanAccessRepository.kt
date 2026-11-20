package org.techascent.aifinder.domain.subscription

data class ScanAccess(
    val userId: String,
    val remainingScans: Long,
) {
    val canScan: Boolean get() = remainingScans > 0
    val isSubscribed: Boolean get() = remainingScans > 0
}

class ScanAccessRequiredException : IllegalStateException("No scans remaining. Choose a scan pack to continue.")

interface ScanAccessRepository {
    /** Cached balance for display only; scanning still verifies the remote wallet. */
    suspend fun cachedOrRefresh(): ScanAccess = refresh()
    suspend fun refresh(): ScanAccess
    suspend fun consume(userId: String, scanId: String): ScanAccess

    /** Saves the purchase to the user's history and adds its scans to the wallet once. */
    suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess
}
