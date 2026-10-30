package org.techascent.aifinder.presentation.scan

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
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

@Composable
actual fun CameraPreview(modifier: Modifier, onUnavailable: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnUnavailable = rememberUpdatedState(onUnavailable)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var preview: Preview? = null
        future.addListener({
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
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, newPreview)
            } catch (_: Exception) {
                currentOnUnavailable.value()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            preview?.let { provider?.unbind(it) }
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
