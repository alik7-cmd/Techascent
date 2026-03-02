package org.techascent.muslim.quran.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class AndroidQuranAudioPlayer : QuranAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var completionListener: (() -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAyahNumber = MutableStateFlow(-1)
    override val currentAyahNumber: StateFlow<Int> = _currentAyahNumber.asStateFlow()

    override fun play(url: String, ayahNumber: Int) {
        try {
            // If same ayah and just paused, resume
            if (_currentAyahNumber.value == ayahNumber && mediaPlayer != null) {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                        _isPlaying.value = true
                        return
                    }
                }
            }

            // Otherwise, stop current and start new
            stop()

            _currentAyahNumber.value = ayahNumber

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    Log.d("QuranAudioPlayer", "Playing ayah $ayahNumber")
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    Log.d("QuranAudioPlayer", "Completed ayah $ayahNumber")
                    completionListener?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuranAudioPlayer", "Error: $what, $extra")
                    _isPlaying.value = false
                    _currentAyahNumber.value = -1
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error playing audio: ${e.message}", e)
            _isPlaying.value = false
            _currentAyahNumber.value = -1
        }
    }

    override fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error pausing: ${e.message}")
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentAyahNumber.value = -1
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error stopping: ${e.message}")
        }
    }

    override fun release() {
        stop()
        completionListener = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}

actual fun createQuranAudioPlayer(): QuranAudioPlayer = AndroidQuranAudioPlayer()
