package org.techascent.aifinder.data.vision

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.toItemDetections

class MockPhotoAnalyzerTest {
    @Test
    fun mockReturnsUsableItemsWithoutAnImageOrNetworkClient() = runBlocking {
        val detections = MockPhotoAnalyzer()
            .analyze(CapturedPhoto("sample", byteArrayOf()))
            .toItemDetections("sample")
        assertEquals(5, detections.size)
        assertTrue(detections.any { it.item.name == "Medal" })
        assertTrue(detections.all { it.photoId == "sample" })
        assertEquals(5, detections.map { it.item.id }.distinct().size)
    }
}
