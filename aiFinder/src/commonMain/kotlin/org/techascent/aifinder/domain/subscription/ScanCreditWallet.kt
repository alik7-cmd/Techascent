package org.techascent.aifinder.domain.subscription

/** Purchase grants are applied by the trusted billing backend, never by the client. */
data class ScanPurchase(
    val purchaseId: String,
    val productId: String,
    val scansGranted: Long,
    val purchasedAt: Long,
    val store: String,
    val status: String,
)

data class ScanCreditWallet(
    val remainingScans: Long = 0,
    val usedScans: Set<String> = emptySet(),
) {
    val activeSubscription: Boolean get() = remainingScans > 0

    init {
        require(remainingScans >= 0) { "Invalid scan balance." }
    }

    fun consume(scanId: String): ScanCreditWallet {
        if (scanId in usedScans) return this
        if (remainingScans == 0L) throw ScanAccessRequiredException()
        return copy(remainingScans = remainingScans - 1, usedScans = usedScans + scanId)
    }
}
