package org.techascent.aifinder.domain.subscription

/** Consumable store products and how many scans each one grants. */
enum class ScanPack(val productId: String, val scans: Long) {
    Small("scan_pack_3", 50),
    Large("scan_pack_5", 200);

    companion object {
        /** Matches plain ids and Play-style `productId:basePlan` ids. */
        fun fromProductId(id: String): ScanPack? =
            entries.firstOrNull { id == it.productId || id.startsWith("${it.productId}:") }
    }
}

/** A completed store transaction as reported by the paywall. */
data class StorePurchase(
    val transactionId: String?,
    val productIds: List<String>,
    val purchaseTime: Long,
    val store: String,
)

/** One entry in the user's purchase history; credited to the wallet at most once. */
data class ScanPurchase(
    val purchaseId: String,
    val productId: String,
    val scansGranted: Long,
    val purchasedAt: Long,
    val store: String,
    val status: String = STATUS_PENDING,
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_CREDITED = "credited"

        /** Returns null when the transaction contains no known scan pack. */
        fun from(purchase: StorePurchase): ScanPurchase? {
            val packs = purchase.productIds.mapNotNull(ScanPack::fromProductId)
            if (packs.isEmpty()) return null
            val productId = purchase.productIds.joinToString(",")
            return ScanPurchase(
                purchaseId = purchase.transactionId?.takeIf { it.isNotBlank() }
                    ?: "${productId}_${purchase.purchaseTime}",
                productId = productId,
                scansGranted = packs.sumOf { it.scans },
                purchasedAt = purchase.purchaseTime,
                store = purchase.store,
            )
        }
    }
}

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
