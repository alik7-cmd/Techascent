package org.techascent.aifinder.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.MAX_ANALYSIS_IMAGE_SIDE
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.posix.memcpy
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.QuartzCore.CATransaction
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

private class CameraSession(val session: AVCaptureSession, val photoOutput: AVCapturePhotoOutput)

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewView(session: AVCaptureSession) : UIView(frame = CGRectZero.readValue()) {
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        backgroundColor = UIColor.blackColor
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

private class PhotoCaptureDelegate(
    private val onResult: (Result<UIImage>) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    override fun captureOutput(output: AVCapturePhotoOutput, didFinishProcessingPhoto: AVCapturePhoto, error: NSError?) {
        val image = didFinishProcessingPhoto.fileDataRepresentation()?.let { UIImage(data = it) }
        onResult(
            when {
                error != null -> Result.failure(IllegalStateException(error.localizedDescription))
                image == null -> Result.failure(IllegalStateException("Couldn't read the captured photo."))
                else -> Result.success(image)
            }
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    controller: CameraCaptureController,
    modifier: Modifier,
    onUnavailable: () -> Unit
) {
    val camera = remember { createCameraSession() }

    if (camera == null) {
        LaunchedEffect(Unit) { onUnavailable() }
        return
    }

    DisposableEffect(camera) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            if (!camera.session.running) camera.session.startRunning()
        }
        controller.captureHandler = { camera.photoOutput.takePhoto() }
        onDispose {
            controller.captureHandler = null
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
                if (camera.session.running) camera.session.stopRunning()
            }
        }
    }

    UIKitView(factory = { CameraPreviewView(camera.session) }, modifier = modifier)
}

private suspend fun AVCapturePhotoOutput.takePhoto(): CapturedPhoto = suspendCancellableCoroutine { cont ->
    // Keep a strong reference until the capture completes.
    var delegate: PhotoCaptureDelegate? = null
    delegate = PhotoCaptureDelegate { result ->
        delegate = null
        result.mapCatching { it.toAnalysisJpeg() }.fold(
            onSuccess = { jpeg ->
                cont.resume(CapturedPhoto("photo-${(NSDate().timeIntervalSince1970 * 1000).toLong()}", jpeg))
            },
            onFailure = { cont.resumeWithException(it) }
        )
    }
    capturePhotoWithSettings(AVCapturePhotoSettings.photoSettings(), delegate!!)
}

// Redrawing applies the EXIF orientation and downscales before upload.
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toAnalysisJpeg(): ByteArray {
    val (width, height) = size.useContents { width to height }
    val scale = minOf(1.0, MAX_ANALYSIS_IMAGE_SIDE / maxOf(width, height))
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width * scale, height * scale), format = format)
    val data = renderer.JPEGDataWithCompressionQuality(0.85) {
        drawInRect(CGRectMake(0.0, 0.0, width * scale, height * scale))
    }
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isEmpty()) throw IllegalStateException("Couldn't encode the captured photo.")
    bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
private fun createCameraSession(): CameraSession? {
    val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return null
    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) ?: return null
    val output = AVCapturePhotoOutput()
    val session = AVCaptureSession()
    if (!session.canAddInput(input) || !session.canAddOutput(output)) return null
    session.beginConfiguration()
    session.sessionPreset = AVCaptureSessionPresetPhoto
    session.addInput(input)
    session.addOutput(output)
    session.commitConfiguration()
    return CameraSession(session, output)
}
