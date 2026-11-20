package org.techascent.aifinder.presentation.scan

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.MAX_ANALYSIS_IMAGE_SIDE

@Composable
actual fun CameraPreview(
    controller: CameraCaptureController,
    modifier: Modifier,
    onUnavailable: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnUnavailable = rememberUpdatedState(onUnavailable)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(1280, 960), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                    )
                    .build()
            )
            .build()
    }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var preview: Preview? = null
        var disposed = false
        future.addListener({
            if (disposed) return@addListener
            try {
                val cameraProvider = future.get().also { provider = it }
                if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    currentOnUnavailable.value()
                    return@addListener
                }
                val newPreview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                    preview = it
                }
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, newPreview, imageCapture
                )
                controller.captureHandler = { imageCapture.takePhoto() }
            } catch (_: Exception) {
                currentOnUnavailable.value()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            controller.captureHandler = null
            provider?.unbind(*listOfNotNull(preview, imageCapture).toTypedArray())
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

// Decode, rotate, downscale and encode on a background thread so the UI stays smooth during capture.
private suspend fun ImageCapture.takePhoto(): CapturedPhoto =
    suspendCancellableCoroutine { cont ->
        takePicture(Dispatchers.Default.asExecutor(), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val jpeg = try {
                    image.toBitmap().rotate(image.imageInfo.rotationDegrees)
                        .scaledDown(MAX_ANALYSIS_IMAGE_SIDE)
                        .toJpeg()
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                    return
                } finally {
                    image.close()
                }
                cont.resume(CapturedPhoto("photo-${System.currentTimeMillis()}", jpeg))
            }

            override fun onError(exception: ImageCaptureException) {
                cont.resumeWithException(exception)
            }
        })
    }

private fun Bitmap.rotate(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also { if (it != this) recycle() }
}

private fun Bitmap.scaledDown(maxSide: Int): Bitmap {
    val largest = maxOf(width, height)
    if (largest <= maxSide) return this
    val scale = maxSide.toFloat() / largest
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        .also { if (it != this) recycle() }
}

private fun Bitmap.toJpeg(): ByteArray = ByteArrayOutputStream().use { out ->
    compress(Bitmap.CompressFormat.JPEG, 85, out)
    recycle()
    out.toByteArray()
}
