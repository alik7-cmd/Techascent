package org.techascent.aifinder.util

import kotlin.io.encoding.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface IosDeviceFilesBridge {
    fun clearSharedFiles(completion: (String?) -> Unit)
    fun inventoryPdf(lines: List<String>, completion: (String?, String?) -> Unit)
    fun generateQr(value: String, label: String, completion: (String?, String?) -> Unit)
    fun decodeQr(base64: String, completion: (String?, String?) -> Unit)
    fun share(name: String, base64: String, mimeType: String, completion: (String?) -> Unit)
}

object IosDeviceFilesBridgeRegistry {
    private var bridge: IosDeviceFilesBridge? = null
    fun install(bridge: IosDeviceFilesBridge) { this.bridge = bridge }
    internal fun requireBridge() = checkNotNull(bridge) { "Install the native device file bridge." }
}

actual suspend fun generateQrPng(value: String, label: String): ByteArray = Base64.decode(deviceCall {
    bridge, done -> bridge.generateQr(value, label, done)
})

actual suspend fun decodeQrPhoto(jpeg: ByteArray): String? = deviceCall {
    bridge, done -> bridge.decodeQr(Base64.encode(jpeg), done)
}.takeIf(String::isNotEmpty)

actual suspend fun shareFile(name: String, data: ByteArray, mimeType: String) {
    deviceCall { bridge, done -> bridge.share(name, Base64.encode(data), mimeType) { done("", it) } }
}

actual suspend fun clearSharedFiles() {
    deviceCall { bridge, done -> bridge.clearSharedFiles { done("", it) } }
}

actual suspend fun createInventoryPdf(lines: List<String>): ByteArray = Base64.decode(deviceCall {
    bridge, done -> bridge.inventoryPdf(lines, done)
})

private suspend fun deviceCall(call: (IosDeviceFilesBridge, (String?, String?) -> Unit) -> Unit): String =
    withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            call(IosDeviceFilesBridgeRegistry.requireBridge()) { result, error ->
                if (continuation.isActive) {
                    if (error != null) continuation.resumeWithException(IllegalStateException(error))
                    else if (result != null) continuation.resume(result)
                    else continuation.resumeWithException(IllegalStateException("Missing device response."))
                }
            }
        }
    }
