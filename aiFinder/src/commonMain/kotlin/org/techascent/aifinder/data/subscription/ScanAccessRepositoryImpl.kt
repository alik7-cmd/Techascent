package org.techascent.aifinder.data.subscription

import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.techascent.aifinder.data.cache.CachedScanWallet
import org.techascent.aifinder.data.cache.ScanWalletDao
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase

class ScanAccessRepositoryImpl(
    private val auth: AuthRepository,
    private val remote: ScanAccessRemoteDataSource,
    private val cache: ScanWalletDao,
) : ScanAccessRepository {
    private val walletMutex = Mutex()

    override suspend fun cachedOrRefresh(): ScanAccess = walletMutex.withLock {
        val userId = requireUser()
        val cached = cache.get(userId)
        check(requireUser() == userId) { "The signed-in user changed. Please try again." }
        cached?.let { ScanAccess(userId, it.remainingScans) } ?: refreshRemote()
    }

    override suspend fun refresh(): ScanAccess = walletMutex.withLock { refreshRemote() }

    private suspend fun refreshRemote(): ScanAccess = withTimeoutOrNull(15_000L) {
        val userId = requireUser()
        cacheAccess(remote.load(userId), userId)
    } ?: error("Checking scan credits timed out. Please try again.")

    override suspend fun consume(userId: String, scanId: String): ScanAccess = walletMutex.withLock {
        withTimeoutOrNull(15_000L) {
            check(requireUser() == userId) { "The signed-in user changed. Please scan again." }
            cacheAccess(remote.consume(userId, scanId), userId)
        } ?: error("Updating scan credits timed out. Please try again.")
    }

    override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess = walletMutex.withLock {
        withTimeoutOrNull(15_000L) {
            val userId = requireUser()
            cacheAccess(remote.recordPurchase(userId, purchase), userId)
        } ?: error("Saving your purchase timed out.")
    }

    private suspend fun cacheAccess(access: ScanAccess, userId: String): ScanAccess {
        check(requireUser() == userId && access.userId == userId) {
            "The signed-in user changed. Please try again."
        }
        cache.upsert(CachedScanWallet(userId, access.remainingScans))
        return access
    }

    private fun requireUser(): String = checkNotNull(auth.currentUser) { "Please sign in to scan." }.id
}
