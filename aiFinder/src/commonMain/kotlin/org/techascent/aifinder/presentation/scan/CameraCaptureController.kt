package org.techascent.aifinder.presentation.scan

import org.techascent.aifinder.domain.vision.CapturedPhoto

/** Bridges the shared "Analyze photo" action to the platform camera preview. */
class CameraCaptureController {
    internal var captureHandler: (suspend () -> CapturedPhoto)? = null

    suspend fun capture(): CapturedPhoto =
        captureHandler?.invoke() ?: throw IllegalStateException("Camera is not ready yet.")
}
