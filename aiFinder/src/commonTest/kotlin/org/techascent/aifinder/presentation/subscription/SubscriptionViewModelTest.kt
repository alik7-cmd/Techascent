package org.techascent.aifinder.presentation.subscription

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.domain.subscription.StorePurchase

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onlyUsersWithCreditOrMasterAccessContinue() = runTest {
        for (access in listOf(ScanAccess("user", 0), ScanAccess("user", 2), ScanAccess("master", 0, true))) {
            val viewModel = SubscriptionViewModel(FakeRepository(access), FakePurchases())
            var allowed = 0
            viewModel.refresh { allowed++ }
            viewModel.refresh { allowed++ }
            runCurrent()
            assertEquals(if (access.canScan) 1 else 0, allowed)
            assertEquals(SubscriptionUiState.Ready(access, purchasesEnabled = !access.canScan), viewModel.state.value)
        }
    }

    @Test
    fun failureIsRetryableAndResetCancelsPendingNavigation() = runTest {
        val repository = FakeRepository(ScanAccess("user", 1))
        repository.fail = true
        val viewModel = SubscriptionViewModel(repository, FakePurchases())
        viewModel.refresh()
        runCurrent()
        assertIs<SubscriptionUiState.Error>(viewModel.state.value)
        repository.fail = false
        var allowed = 0
        viewModel.refresh { allowed++ }
        viewModel.reset()
        runCurrent()
        assertEquals(0, allowed)
        viewModel.refresh { allowed++ }
        runCurrent()
        assertEquals(1, allowed)
    }

    @Test
    fun outOfCreditUsersGetThePaywallOnlyWhenIdentified() = runTest {
        val purchases = FakePurchases()
        val viewModel = SubscriptionViewModel(FakeRepository(ScanAccess("uid", 0)), purchases)
        viewModel.refresh()
        runCurrent()
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 0), purchasesEnabled = true), viewModel.state.value)
        assertEquals(listOf("uid"), purchases.identified)

        purchases.failIdentify = true
        viewModel.refresh()
        runCurrent()
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 0)), viewModel.state.value)

        val unconfigured = SubscriptionViewModel(FakeRepository(ScanAccess("uid", 0)), FakePurchases(available = false))
        unconfigured.refresh()
        runCurrent()
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 0)), unconfigured.state.value)
    }

    @Test
    fun purchaseWaitsForTheWebhookCreditThenContinues() = runTest {
        val repository = FakeRepository(ScanAccess("uid", 0))
        val viewModel = SubscriptionViewModel(repository, FakePurchases())
        viewModel.refresh()
        runCurrent()
        var allowed = 0
        viewModel.onPurchaseCompleted { allowed++ }
        runCurrent()
        assertEquals(SubscriptionUiState.AddingScans, viewModel.state.value)
        repository.value = ScanAccess("uid", 5)
        advanceUntilIdle()
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 5), purchasesEnabled = true), viewModel.state.value)
        assertEquals(1, allowed)
    }

    @Test
    fun purchaseNotCreditedInTimeIsReportedAsPending() = runTest {
        val viewModel = SubscriptionViewModel(FakeRepository(ScanAccess("uid", 0)), FakePurchases())
        viewModel.refresh()
        runCurrent()
        var allowed = 0
        viewModel.onPurchaseCompleted { allowed++ }
        advanceUntilIdle()
        assertEquals(
            SubscriptionUiState.Ready(ScanAccess("uid", 0), purchasesEnabled = true, creditsPending = true),
            viewModel.state.value
        )
        assertEquals(0, allowed)
    }

    @Test
    fun purchaseIsRecordedAndCreditedThenContinues() = runTest {
        val repository = FakeRepository(ScanAccess("uid", 0))
        val viewModel = SubscriptionViewModel(repository, FakePurchases())
        viewModel.refresh()
        runCurrent()
        var allowed = 0
        viewModel.onPurchaseCompleted(StorePurchase("GPA.1", listOf("scan_pack_5"), 42L, "play_store")) { allowed++ }
        advanceUntilIdle()
        assertEquals(
            listOf(ScanPurchase("GPA.1", "scan_pack_5", 5, 42L, "play_store")),
            repository.recorded
        )
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 5), purchasesEnabled = true), viewModel.state.value)
        assertEquals(1, allowed)
    }

    @Test
    fun failedRecordFallsBackToPollingForTheCredit() = runTest {
        val repository = FakeRepository(ScanAccess("uid", 0)).apply { failRecord = true }
        val viewModel = SubscriptionViewModel(repository, FakePurchases())
        viewModel.refresh()
        runCurrent()
        viewModel.onPurchaseCompleted(StorePurchase("t1", listOf("scan_pack_3"), 1L, "play_store"))
        runCurrent()
        assertEquals(SubscriptionUiState.AddingScans, viewModel.state.value)
        repository.value = ScanAccess("uid", 3)
        advanceUntilIdle()
        assertEquals(SubscriptionUiState.Ready(ScanAccess("uid", 3), purchasesEnabled = true), viewModel.state.value)
    }

    @Test
    fun storePurchasesMapToScanPacks() {
        assertEquals(3, ScanPurchase.from(StorePurchase("a", listOf("scan_pack_3"), 1L, "s"))?.scansGranted)
        assertEquals(5, ScanPurchase.from(StorePurchase("a", listOf("scan_pack_5:base"), 1L, "s"))?.scansGranted)
        assertEquals("scan_pack_3_7", ScanPurchase.from(StorePurchase(null, listOf("scan_pack_3"), 7L, "s"))?.purchaseId)
        assertEquals(null, ScanPurchase.from(StorePurchase("a", listOf("premium_monthly"), 1L, "s")))
    }

    private class FakePurchases(private val available: Boolean = true) : PurchaseGateway {
        val identified = mutableListOf<String>()
        var failIdentify = false
        override fun prepare() = available
        override suspend fun identify(userId: String) {
            check(!failIdentify) { "Store offline" }
            identified += userId
        }
        override suspend fun reset() = Unit
    }

    private class FakeRepository(var value: ScanAccess) : ScanAccessRepository {
        var fail = false
        override suspend fun refresh(): ScanAccess {
            check(!fail) { "Offline" }
            return value
        }
        override suspend fun consume(userId: String, scanId: String) = error("Not used")
        val recorded = mutableListOf<ScanPurchase>()
        var failRecord = false
        override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess {
            check(!failRecord) { "Offline" }
            recorded += purchase
            value = value.copy(remainingScans = value.remainingScans + purchase.scansGranted)
            return value
        }
    }
}
