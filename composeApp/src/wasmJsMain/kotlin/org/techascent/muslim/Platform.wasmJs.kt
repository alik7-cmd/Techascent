package org.techascent.muslim

import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/*class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()*/

actual fun playBeep(){}

actual fun performHapticFeedback(){
    window.navigator.vibrate(50)
}


actual fun showNativeResetDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
){

}

actual fun getQiblaDirection(currentLat: Double, currentLng: Double): Flow<Float> = flowOf(0f)