package org.techascent.aifinder.data.vision

import kotlinx.coroutines.test.runTest
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.ItemDetectionPrompt
import org.techascent.aifinder.domain.vision.toItemDetections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakePhotoAnalyzerTest {
    @Test
    fun returnsDeterministicUsableSamplesWithoutImageDataOrApiCredentials() = runTest {
        val analyzer = FakePhotoAnalyzer()
        val results = analyzer.analyze(CapturedPhoto("first", byteArrayOf()))

        assertEquals(listOf("HDMI cable", "Passport", "Key"), results.map { it.label })
        assertEquals(results, analyzer.analyze(CapturedPhoto("second", byteArrayOf(1, 2))))
        results.forEach {
            assertTrue(it.confidence in 0f..1f)
            assertTrue(it.category in ItemDetectionPrompt.categories)
            assertNull(it.bounds)
        }
        val detections = results.toItemDetections("first")
        assertEquals(3, detections.size)
        assertTrue(detections.all { it.photoId == "first" })
    }
}
