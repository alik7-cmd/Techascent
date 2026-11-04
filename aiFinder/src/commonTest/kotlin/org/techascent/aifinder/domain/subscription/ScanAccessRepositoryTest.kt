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

class ScanAccessRepositoryTest {
    @Test
    fun hardCodedMasterBypassesRemoteReadsAndDeductionsButNotUserChecks() = runTest {
        val auth = FakeAuth()
        val repository = ScanAccessRepositoryImpl(auth, object : ScanAccessRemoteDataSource {
            override suspend fun load(userId: String): ScanAccess = error("Must not read Firebase")
            override suspend fun consume(userId: String, scanId: String): ScanAccess = error("Must not deduct credits")
            override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) = error("Not used")
        }, masterUserIds = setOf("user"))
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
        })
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
        })
        repository.refresh()
        auth.currentUser = AuthUser("other", null, null, null)
        repository.refresh()
        assertEquals(listOf("user", "other"), loaded)
        assertFailsWith<IllegalStateException> { repository.consume("user", "scan") }
        assertEquals(0, charges)
        auth.currentUser = null
        assertFailsWith<IllegalStateException> { repository.refresh() }
    }

    private class FakeAuth : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("user", null, null, null)
        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Not used")
        override suspend fun signOut() { currentUser = null }
    }
}
