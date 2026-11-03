package org.techascent.aifinder.domain.subscription

import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import org.techascent.aifinder.domain.vision.toItemDetections

class MeteredPhotoAnalyzer(
    private val analyzer: PhotoAnalyzer,
    private val access: ScanAccessRepository,
) : PhotoAnalyzer {
    override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
        val current = access.refresh()
        if (!current.canScan) throw ScanAccessRequiredException()
        val recognized = analyzer.analyze(photo)
        if (recognized.toItemDetections(photo.id).isNotEmpty()) {
            access.consume(current.userId, photo.id)
        }
        return recognized
    }
}
