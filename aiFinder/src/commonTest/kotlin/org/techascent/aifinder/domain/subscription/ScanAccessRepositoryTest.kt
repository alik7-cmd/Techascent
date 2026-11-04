package org.techascent.aifinder.domain.subscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.data.subscription.ScanAccessRemoteDataSource
import org.techascent.aifinder.data.subscription.ScanAccessRepositoryImpl
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.data.cache.CachedScanWallet
import org.techascent.aifinder.data.cache.ScanWalletDao

class ScanAccessRepositoryTest {
    @Test
    fun hardCodedMasterBypassesRemoteReadsAndDeductionsButNotUserChecks() = runTest {
        val auth = FakeAuth()
        val repository = ScanAccessRepositoryImpl(auth, object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess = error("Must not read Firebase")
            override suspend fun consume(userId: String, scanId: String): ScanAccess = error("Must not deduct credits")
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) = error("Not used")
        }, cache = FakeCache(), masterUserIds = setOf("user"))
        assertEquals(ScanAccess("user", 0, true), repository.refresh())
        assertEquals(ScanAccess("user", 0, true), repository.consume("user", "scan"))
        auth.currentUser = AuthUser("other", null, null, null)
        assertFailsWith<IllegalStateException> { repository.consume("user", "scan") }
        assertFailsWith<IllegalStateException> { repository.refresh() }
    }

    @Test
    fun timeoutBecomesAnErrorInsteadOfLeavingUiLoading() = runTest {
        val repository = ScanAccessRepositoryImpl(FakeAuth(), object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess = CompletableDeferred<ScanAccess>().await()
            override suspend fun consume(userId: String, scanId: String): ScanAccess =
                CompletableDeferred<ScanAccess>().await()
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) = error("Not used")
        }, cache = FakeCache())
        assertFailsWith<IllegalStateException> { repository.refresh() }
        assertEquals(15_000L, testScheduler.currentTime)
        assertFailsWith<IllegalStateException> { repository.consume("user", "scan") }
        assertEquals(30_000L, testScheduler.currentTime)
    }

    @Test
    fun readsAreUserScopedAndPreviousUserCannotBeCharged() = runTest {
        val auth = FakeAuth()
        val loaded = mutableListOf<String>()
        var charges = 0
        val repository = ScanAccessRepositoryImpl(auth, object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess {
                loaded += userId
                return ScanAccess(userId, 1)
            }
            override suspend fun consume(userId: String, scanId: String): ScanAccess {
                charges++
                return ScanAccess(userId, 0)
            }
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) = error("Not used")
        }, cache = FakeCache())
        repository.refresh()
        auth.currentUser = AuthUser("other", null, null, null)
        repository.refresh()
        assertEquals(listOf("user", "other"), loaded)
        assertFailsWith<IllegalStateException> { repository.consume("user", "scan") }
        assertEquals(0, charges)
        auth.currentUser = null
        assertFailsWith<IllegalStateException> { repository.refresh() }
    }

    @Test
    fun balanceIsCacheFirstAndUpdatedAfterConsumptionAndPurchase() = runTest {
        val auth = FakeAuth()
        val cache = FakeCache()
        var loads = 0
        val repository = ScanAccessRepositoryImpl(auth, object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess {
                loads++
                return ScanAccess(userId, 1)
            }
            override suspend fun consume(userId: String, scanId: String) = ScanAccess(userId, 0)
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) =
                ScanAccess(userId, purchase.scansGranted)
        }, cache)

        assertEquals(1, repository.cachedOrRefresh().remainingScans)
        assertEquals(1, repository.cachedOrRefresh().remainingScans)
        assertEquals(1, loads)
        repository.consume("user", "scan")
        assertEquals(0, repository.cachedOrRefresh().remainingScans)
        assertEquals(1, loads) // A zero balance is a valid cache hit.
        repository.recordPurchase(ScanPurchase("purchase", "scan_pack_5", 5, 1, "play_store"))
        assertEquals(5, repository.cachedOrRefresh().remainingScans)
        auth.currentUser = AuthUser("other", null, null, null)
        assertEquals("other", repository.cachedOrRefresh().userId)
        assertEquals(2, loads)
    }

    @Test
    fun failedRemoteLoadDoesNotCacheAZeroBalance() = runTest {
        val cache = FakeCache()
        val repository = ScanAccessRepositoryImpl(FakeAuth(), object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess = error("Offline")
            override suspend fun consume(userId: String, scanId: String): ScanAccess = error("Unused")
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase): ScanAccess = error("Unused")
        }, cache)
        assertFailsWith<IllegalStateException> { repository.cachedOrRefresh() }
        assertEquals(null, cache.get("user"))
    }

    private class FakeCache : ScanWalletDao {
        private val wallets = mutableMapOf<String, CachedScanWallet>()
        override suspend fun get(userId: String) = wallets[userId]
        override suspend fun upsert(wallet: CachedScanWallet) { wallets[wallet.userId] = wallet }
        override suspend fun deleteForUser(userId: String) { wallets.remove(userId) }
    }

    private class FakeAuth : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("user", null, null, null)
        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Not used")
        override suspend fun signOut() { currentUser = null }
    }
}
