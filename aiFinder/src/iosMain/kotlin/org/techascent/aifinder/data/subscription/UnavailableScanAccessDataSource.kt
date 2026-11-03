package org.techascent.aifinder.data.subscription

import org.techascent.aifinder.domain.subscription.ScanAccess

class UnavailableScanAccessDataSource : ScanAccessRemoteDataSource {
    override suspend fun load(userId: String): ScanAccess =
        error("Firebase scan credits are not available on iOS yet.")

    override suspend fun consume(userId: String, scanId: String): ScanAccess =
        error("Firebase scan credits are not available on iOS yet.")
}
