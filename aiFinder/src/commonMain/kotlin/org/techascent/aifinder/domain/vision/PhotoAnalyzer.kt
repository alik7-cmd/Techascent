package org.techascent.aifinder.domain.vision

/** A photo captured by the platform camera, already oriented, downscaled and JPEG-encoded. */
class CapturedPhoto(val id: String, val jpegBytes: ByteArray)

/**
 * A raw recognition result.
 * [bounds] are normalized to 0..1 as `[left, top, right, bottom]`, or null when unknown.
 */
data class RecognizedObject(
    val label: String,
    val confidence: Float,
    val bounds: List<Float>? = null,
    val category: String? = null
)

interface PhotoAnalyzer {
    suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject>
}

/** Max longest side (px) of photos sent for analysis; keeps uploads small and latency low. */
const val MAX_ANALYSIS_IMAGE_SIDE = 1280
