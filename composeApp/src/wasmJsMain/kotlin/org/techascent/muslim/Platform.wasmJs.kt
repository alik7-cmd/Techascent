package org.techascent.muslim

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

actual fun changeAppLocale(langCode: String) {
    // No-op on wasmJs
}

@Composable
actual fun rememberAppLocale(): AppLang {
    return remember { AppLang.English }
}

actual class UrlLauncher {
    actual fun openAppSettings() {
        // No-op on wasmJs
    }

    actual fun openLanguageSettings() {
        // No-op on wasmJs
    }
}

@Composable
actual fun rememberUrlLauncher(): UrlLauncher {
    return remember { UrlLauncher() }
}

