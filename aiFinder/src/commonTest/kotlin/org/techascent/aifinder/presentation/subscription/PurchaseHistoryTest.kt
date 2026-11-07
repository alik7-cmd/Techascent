package org.techascent.aifinder.presentation.subscription

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.techascent.aifinder.data.subscription.PurchaseHistoryRemote
import org.techascent.aifinder.data.subscription.PurchaseHistoryRepository
import org.techascent.aifinder.domain.auth.*
import org.techascent.aifinder.domain.subscription.ScanPurchase
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseHistoryTest {
    private class Auth : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("user", null, null, null)
        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Not used")
        override suspend fun signOut() { currentUser = null }
    }

    private fun repository(auth: Auth = Auth(), load: suspend (String) -> List<ScanPurchase>) =
        PurchaseHistoryRepository(auth, object : PurchaseHistoryRemote {
            override suspend fun load(userId: String) = load.invoke(userId)
        })

    @Test
    fun loadsOnlyCurrentUserAndSortsNewestFirst() = runTest {
        val older = ScanPurchase("old", "scan_pack_3", 3, 10, "PLAY_STORE")
        val newer = older.copy(purchaseId = "new", purchasedAt = 20, status = ScanPurchase.STATUS_CREDITED)
        val result = repository {
            assertEquals("user", it)
            listOf(older, newer)
        }.load()
        assertEquals(listOf(newer, older), result)
    }

    @Test
    fun signedOutUserNeverQueriesHistory() = runTest {
        val auth = Auth().apply { currentUser = null }
        assertFailsWith<IllegalStateException> {
            repository(auth) { error("Must not query") }.load()
        }
    }

    @Test
    fun rejectsResultsIfAccountChangesDuringRequest() = runTest {
        val auth = Auth()
        assertFailsWith<IllegalStateException> {
            repository(auth) {
                auth.currentUser = AuthUser("other", null, null, null)
                emptyList()
            }.load()
        }
    }

    @Test
    fun timeoutBecomesRetryableError() = runTest {
        assertFailsWith<IllegalStateException> {
            repository { CompletableDeferred<List<ScanPurchase>>().await() }.load()
        }
        assertEquals(15_000L, testScheduler.currentTime)
    }

    @Test
    fun viewModelShowsErrorThenEmptyStateOnRetry() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            var fail = true
            val model = PurchaseHistoryViewModel(repository {
                if (fail) error("Offline")
                emptyList()
            })
            model.load()
            assertEquals(PurchaseHistoryState.Error, model.state.value)
            fail = false
            model.load()
            assertEquals(PurchaseHistoryState.Ready(emptyList()), model.state.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
