package org.techascent.aifinder.data.vision

import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject

/** Temporary offline results for developing the scan/review flow without API usage. */
class MockPhotoAnalyzer : PhotoAnalyzer {
    override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
        return listOf(
            RecognizedObject("Medal", 0.94f, category = "Accessories"),
            RecognizedObject("HDMI cable", 0.92f, category = "Electronics"),
            RecognizedObject("AA battery", 0.89f, category = "Electronics"),
            RecognizedObject("Scissors", 0.91f, category = "Tools"),
            RecognizedObject("Notebook", 0.87f, category = "Documents")
        )
    }
}
