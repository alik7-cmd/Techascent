package org.techascent.aifinder.data.subscription

import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanPurchase

class UnavailableScanAccessDataSource : ScanAccessRemoteDataSource {
    override suspend fun load(userId: String): ScanAccess =
        error("Firebase scan credits are not available on iOS yet.")

    override suspend fun consume(userId: String, scanId: String): ScanAccess =
        error("Firebase scan credits are not available on iOS yet.")

    override suspend fun recordPurchase(userId: String, purchase: ScanPurchase): ScanAccess =
        error("Firebase scan credits are not available on iOS yet.")
}
