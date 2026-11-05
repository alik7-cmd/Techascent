package org.techascent.aifinder.presentation.scan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.subscription.MeteredPhotoAnalyzer
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ScanPurchaseFlowTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun zeroCreditsOpensPurchaseThenResumesTheSamePhotoWithoutRecapture() = runTest {
        val access = Wallet()
        val analyzer = Analyzer()
        val vm = ScanViewModel(MeteredPhotoAnalyzer(analyzer, access), access)
        var captures = 0
        val photo = CapturedPhoto("photo", byteArrayOf(1))
        val camera = CameraCaptureController().apply { captureHandler = { captures++; photo } }
        vm.loadBalance()
        runCurrent()
        vm.analyze(camera)
        runCurrent()
        assertTrue(vm.content().requiresPurchase)
        assertFalse(vm.content().isAnalyzing)
        assertEquals(0, analyzer.calls)
        assertEquals(0, access.spent)
        access.balance = 3
        vm.resumeAfterPurchase()
        vm.resumeAfterPurchase()
        runCurrent()
        assertSame(photo, vm.detections.value?.photo)
        assertEquals(1, captures)
        assertEquals(1, analyzer.calls)
        assertEquals(1, access.spent)
        assertEquals(2, vm.content().access?.remainingScans)
        assertFalse(vm.content().requiresPurchase)
    }

    @Test
    fun staleZeroBalanceDoesNotBlockAnAuthoritativelyAllowedScan() = runTest {
        val access = Wallet().apply { balance = 1 }
        val vm = ScanViewModel(MeteredPhotoAnalyzer(Analyzer(), access), access)
        vm.analyze(CameraCaptureController().apply {
            captureHandler = { CapturedPhoto("photo", byteArrayOf()) }
        })
        runCurrent()
        assertNotNull(vm.detections.value)
        assertEquals(1, access.spent)
    }

    @Test
    fun dismissOrLeaveDiscardsPendingPhotoAndPreventsLateResume() = runTest {
        val access = Wallet()
        val vm = ScanViewModel(MeteredPhotoAnalyzer(Analyzer(), access), access)
        val camera = CameraCaptureController().apply {
            captureHandler = { CapturedPhoto("photo", byteArrayOf()) }
        }
        vm.analyze(camera)
        runCurrent()
        vm.dismissPurchase()
        access.balance = 1
        vm.resumeAfterPurchase()
        runCurrent()
        assertNull(vm.detections.value)
        assertEquals(0, access.spent)
        access.balance = 0
        vm.analyze(camera)
        runCurrent()
        vm.cancelAnalysis()
        access.balance = 1
        vm.resumeAfterPurchase()
        runCurrent()
        assertNull(vm.detections.value)
        assertFalse(vm.content().requiresPurchase)
    }

    @Test
    fun remoteErrorsAreShownWithoutOpeningPurchase() = runTest {
        val access = Wallet().apply { offline = true }
        val vm = ScanViewModel(MeteredPhotoAnalyzer(Analyzer(), access), access)
        vm.analyze(CameraCaptureController().apply {
            captureHandler = { CapturedPhoto("photo", byteArrayOf()) }
        })
        runCurrent()
        assertEquals("Offline", vm.content().errorMessage)
        assertFalse(vm.content().requiresPurchase)
        assertNull(vm.detections.value)
    }

    @Test
    fun cancelledCaptureDoesNotPublishResultsOrSpendCredits() = runTest {
        val access = Wallet().apply { balance = 1 }
        val vm = ScanViewModel(MeteredPhotoAnalyzer(Analyzer(), access), access)
        vm.analyze(CameraCaptureController().apply { captureHandler = { awaitCancellation() } })
        runCurrent()
        vm.cancelAnalysis()
        runCurrent()
        assertFalse(vm.content().isAnalyzing)
        assertEquals(0, access.spent)
        assertNull(vm.detections.value)
    }

    private fun ScanViewModel.content() = (state.value as ScanUiState.Success).data

    private class Analyzer : PhotoAnalyzer {
        var calls = 0
        override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
            calls++
            return listOf(RecognizedObject("Key", 0.9f))
        }
    }

    private class Wallet : ScanAccessRepository {
        var balance = 0L
        var spent = 0
        var offline = false
        override suspend fun cachedOrRefresh() = ScanAccess("user", balance)
        override suspend fun refresh(): ScanAccess {
            check(!offline) { "Offline" }
            return ScanAccess("user", balance)
        }
        override suspend fun consume(userId: String, scanId: String): ScanAccess {
            spent++
            balance--
            return ScanAccess("user", balance)
        }
        override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess = error("Unused")
    }
}
