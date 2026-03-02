package org.techascent.muslim.quran.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class WasmJsQuranAudioPlayer : QuranAudioPlayer {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAyahNumber = MutableStateFlow(-1)
    override val currentAyahNumber: StateFlow<Int> = _currentAyahNumber.asStateFlow()

    override fun play(url: String, ayahNumber: Int) {
        // WasmJs audio playback not implemented
        println("QuranAudioPlayer: play not supported on wasmJs")
    }

    override fun pause() {
        // No-op
    }

    override fun stop() {
        _isPlaying.value = false
        _currentAyahNumber.value = -1
    }

    override fun release() {
        stop()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        // No-op
    }
}

actual fun createQuranAudioPlayer(): QuranAudioPlayer = WasmJsQuranAudioPlayer()
