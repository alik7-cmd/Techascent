package org.techascent.aifinder.domain.subscription

data class ScanAccess(
    val userId: String,
    val remainingScans: Long,
    val isMaster: Boolean = false,
) {
    val canScan: Boolean get() = isMaster || remainingScans > 0
    val isSubscribed: Boolean get() = remainingScans > 0
}

class ScanAccessRequiredException : IllegalStateException("No scans remaining. Choose a scan pack to continue.")

interface ScanAccessRepository {
    suspend fun refresh(): ScanAccess
    suspend fun consume(userId: String, scanId: String): ScanAccess

    /** Saves the purchase to the user's history and adds its scans to the wallet once. */
    suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess
}
