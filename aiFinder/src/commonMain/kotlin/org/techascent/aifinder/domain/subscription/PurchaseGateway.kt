package org.techascent.aifinder.domain.subscription

/** Store purchases for scan packs. Credits themselves are granted server-side into the scan wallet. */
interface PurchaseGateway {
    /** Prepares the store SDK; false when purchases aren't configured for this platform. */
    fun prepare(): Boolean

    /** Ties purchases to the Firebase user so the backend can credit the right wallet. */
    suspend fun identify(userId: String)

    suspend fun reset()
}
