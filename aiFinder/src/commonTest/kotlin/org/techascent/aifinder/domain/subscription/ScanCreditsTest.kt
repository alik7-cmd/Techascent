package org.techascent.aifinder.domain.subscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject

class ScanCreditsTest {
    @Test
    fun lastCreditEndsSubscriptionAndDuplicateConsumptionIsFree() {
        assertTrue(ScanCreditWallet(1).activeSubscription)
        val wallet = ScanCreditWallet(1).consume("scan")
        assertFalse(wallet.activeSubscription)
        assertEquals(0L, wallet.remainingScans)
        assertEquals(wallet, wallet.consume("scan"))
        assertFailsWith<ScanAccessRequiredException> { wallet.consume("another") }
        assertFalse(ScanAccess("user", wallet.remainingScans).isSubscribed)
        assertFalse(ScanAccess("user", 0).canScan)
        assertTrue(ScanAccess("master", 0, true).canScan)
    }

    @Test
    fun successfulRecognitionConsumesExactlyOneCredit() = runTest {
        val access = FakeAccess()
        val analyzer = MeteredPhotoAnalyzer(FakeAnalyzer(), access)
        assertEquals(1, analyzer.analyze(CapturedPhoto("scan", byteArrayOf())).size)
        assertEquals(listOf("user" to "scan"), access.consumed)
    }

    @Test
    fun emptyAndFailedAnalysesDoNotConsumeCredits() = runTest {
        val access = FakeAccess()
        MeteredPhotoAnalyzer(FakeAnalyzer(emptyList()), access).analyze(CapturedPhoto("empty", byteArrayOf()))
        assertFailsWith<IllegalStateException> {
            MeteredPhotoAnalyzer(object : PhotoAnalyzer {
                override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> = error("AI unavailable")
            }, access).analyze(CapturedPhoto("failed", byteArrayOf()))
        }
        assertTrue(access.consumed.isEmpty())
    }

    @Test
    fun noCreditBlocksAiCall() = runTest {
        val analyzer = FakeAnalyzer()
        assertFailsWith<ScanAccessRequiredException> {
            MeteredPhotoAnalyzer(analyzer, FakeAccess(ScanAccess("user", 0)))
                .analyze(CapturedPhoto("scan", byteArrayOf()))
        }
        assertEquals(0, analyzer.calls)
    }

    @Test
    fun failedAccessCheckBlocksAiCall() = runTest {
        val analyzer = FakeAnalyzer()
        val access = object : ScanAccessRepository {
            override suspend fun refresh(): ScanAccess = error("Wallet unavailable")
            override suspend fun consume(userId: String, scanId: String): ScanAccess = error("Must not spend")
            override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess = error("Unused")
        }
        assertFailsWith<IllegalStateException> {
            MeteredPhotoAnalyzer(analyzer, access).analyze(CapturedPhoto("scan", byteArrayOf()))
        }
        assertEquals(0, analyzer.calls)
    }

    @Test
    fun balanceWriteFailureDoesNotReturnSuccess() = runTest {
        val access = FakeAccess(failConsume = true)
        assertFailsWith<IllegalStateException> {
            MeteredPhotoAnalyzer(FakeAnalyzer(), access).analyze(CapturedPhoto("scan", byteArrayOf()))
        }
    }

    private class FakeAnalyzer(
        private val results: List<RecognizedObject> = listOf(RecognizedObject("Cable", 0.9f))
    ) : PhotoAnalyzer {
        var calls = 0
        override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
            calls++
            return results
        }
    }

    private class FakeAccess(
        private val value: ScanAccess = ScanAccess("user", 1),
        private val failConsume: Boolean = false,
    ) : ScanAccessRepository {
        val consumed = mutableListOf<Pair<String, String>>()
        override suspend fun refresh() = value
        override suspend fun consume(userId: String, scanId: String): ScanAccess {
            check(!failConsume) { "Write failed" }
            consumed += userId to scanId
            return value.copy(remainingScans = 0)
        }
        override suspend fun recordPurchase(purchase: ScanPurchase) = error("Not used")
    }
}
