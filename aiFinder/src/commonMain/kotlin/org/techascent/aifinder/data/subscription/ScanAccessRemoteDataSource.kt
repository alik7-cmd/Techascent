package org.techascent.aifinder.data.subscription

import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanPurchase

interface ScanAccessRemoteDataSource {
    suspend fun load(userId: String): ScanAccess
    suspend fun consume(userId: String, scanId: String): ScanAccess
    suspend fun recordPurchase(userId: String, purchase: ScanPurchase): ScanAccess
}
