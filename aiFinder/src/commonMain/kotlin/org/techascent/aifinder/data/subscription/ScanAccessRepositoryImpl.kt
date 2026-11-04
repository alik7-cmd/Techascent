package org.techascent.aifinder.data.subscription

import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.techascent.aifinder.data.cache.CachedScanWallet
import org.techascent.aifinder.data.cache.ScanWalletDao
import org.techascent.aifinder.config.MasterUsers
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase

class ScanAccessRepositoryImpl(
    private val auth: AuthRepository,
    private val remote: ScanAccessRemoteDataSource,
    private val cache: ScanWalletDao,
    private val masterUserIds: Set<String> = MasterUsers.userIds,
) : ScanAccessRepository {
    private val walletMutex = Mutex()

    override suspend fun cachedOrRefresh(): ScanAccess = walletMutex.withLock {
        val userId = requireUser()
        if (userId in masterUserIds) return@withLock ScanAccess(userId, 0, isMaster = true)
        val cached = cache.get(userId)
        check(requireUser() == userId) { "The signed-in user changed. Please try again." }
        cached?.let { ScanAccess(userId, it.remainingScans) } ?: refreshRemote()
    }

    override suspend fun refresh(): ScanAccess = walletMutex.withLock { refreshRemote() }

    private suspend fun refreshRemote(): ScanAccess = withTimeoutOrNull(15_000L) {
        val userId = requireUser()
        if (userId in masterUserIds) return@withTimeoutOrNull ScanAccess(userId, 0, isMaster = true)
        cacheAccess(remote.load(userId), userId)
    } ?: error("Checking scan credits timed out. Please try again.")

    override suspend fun consume(userId: String, scanId: String): ScanAccess = walletMutex.withLock {
        withTimeoutOrNull(15_000L) {
            check(requireUser() == userId) { "The signed-in user changed. Please scan again." }
            if (userId in masterUserIds) return@withTimeoutOrNull ScanAccess(userId, 0, isMaster = true)
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
