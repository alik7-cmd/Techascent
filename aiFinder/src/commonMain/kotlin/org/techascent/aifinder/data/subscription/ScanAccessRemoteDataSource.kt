package org.techascent.aifinder.data.subscription

import org.techascent.aifinder.domain.subscription.ScanAccess

interface ScanAccessRemoteDataSource {
    suspend fun load(userId: String): ScanAccess
    suspend fun consume(userId: String, scanId: String): ScanAccess
}
