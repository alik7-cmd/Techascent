package org.techascent.muslim.quran.audio

import kotlinx.coroutines.flow.StateFlow

interface QuranAudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentAyahNumber: StateFlow<Int>

    fun play(url: String, ayahNumber: Int)
    fun pause()
    fun stop()
    fun release()

    fun setOnCompletionListener(listener: () -> Unit)
}

expect fun createQuranAudioPlayer(): QuranAudioPlayer
