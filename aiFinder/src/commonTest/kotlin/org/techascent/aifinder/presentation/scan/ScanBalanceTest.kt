package org.techascent.aifinder.presentation.scan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScanBalanceTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadingBalanceUsesCacheAndShowsTheUpdatedBalanceOnReturn() = runTest {
        val access = FakeAccess()
        val viewModel = ScanViewModel(UnusedAnalyzer, access)
        viewModel.loadBalance()
        assertTrue(viewModel.content().isLoadingBalance)
        runCurrent()
        assertEquals(5, viewModel.content().access?.remainingScans)
        assertFalse(viewModel.content().isLoadingBalance)

        access.balance = ScanAccess("user", 0)
        viewModel.loadBalance()
        runCurrent()
        assertEquals(0, viewModel.content().access?.remainingScans)
        assertFalse(viewModel.content().access!!.canScan)
    }

    @Test
    fun failureDoesNotDisplayAnInventedZeroAndCanBeRetried() = runTest {
        val access = FakeAccess().apply { fail = true }
        val viewModel = ScanViewModel(UnusedAnalyzer, access)
        viewModel.loadBalance()
        runCurrent()
        assertTrue(viewModel.content().balanceError)
        assertEquals(null, viewModel.content().access)

        access.fail = false
        viewModel.loadBalance()
        runCurrent()
        assertFalse(viewModel.content().balanceError)
        assertEquals(5, viewModel.content().access?.remainingScans)
    }

    private fun ScanViewModel.content() = (state.value as ScanUiState.Success).data

    private class FakeAccess : ScanAccessRepository {
        var balance = ScanAccess("user", 5)
        var fail = false
        override suspend fun cachedOrRefresh(): ScanAccess {
            check(!fail) { "Offline" }
            return balance
        }
        override suspend fun refresh(): ScanAccess = error("Must use the cache-first method")
        override suspend fun consume(userId: String, scanId: String): ScanAccess = error("Unused")
        override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess = error("Unused")
    }

    private object UnusedAnalyzer : PhotoAnalyzer {
        override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> = error("Unused")
    }
}
