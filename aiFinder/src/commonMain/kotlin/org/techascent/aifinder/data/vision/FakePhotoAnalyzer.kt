package org.techascent.aifinder.data.vision

import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import org.techascent.aifinder.util.logDebug

class FakePhotoAnalyzer : PhotoAnalyzer {
    override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
        logDebug("AiFinderScan", "Fake analysis enabled: returning sample items without calling Vertex AI.")
        return listOf(
            RecognizedObject(label = "HDMI cable", confidence = 0.96f, category = "Electronics"),
            RecognizedObject(label = "Passport", confidence = 0.94f, category = "Documents"),
            RecognizedObject(label = "Key", confidence = 0.91f, category = "Accessories"),
        )
    }
}
