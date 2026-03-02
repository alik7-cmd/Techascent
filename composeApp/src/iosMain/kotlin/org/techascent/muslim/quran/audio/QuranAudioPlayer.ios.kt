package org.techascent.muslim.quran.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.darwin.NSObject
import platform.AVFAudio.AVAudioPlayerDelegateProtocol

@OptIn(ExperimentalForeignApi::class)
private class IOSQuranAudioPlayer : QuranAudioPlayer {

    private var audioPlayer: AVAudioPlayer? = null
    private var completionListener: (() -> Unit)? = null
    private var delegate: AudioPlayerDelegate? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAyahNumber = MutableStateFlow(-1)
    override val currentAyahNumber: StateFlow<Int> = _currentAyahNumber.asStateFlow()

    override fun play(url: String, ayahNumber: Int) {
        try {
            // If same ayah and just paused, resume
            if (_currentAyahNumber.value == ayahNumber && audioPlayer != null) {
                audioPlayer?.let {
                    if (!it.playing) {
                        it.play()
                        _isPlaying.value = true
                        return
                    }
                }
            }

            stop()
            _currentAyahNumber.value = ayahNumber

            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, null)
            session.setActive(true, null)

            val nsUrl = NSURL.URLWithString(url) ?: return
            val data = NSData.dataWithContentsOfURL(nsUrl) ?: return

            audioPlayer = AVAudioPlayer(data = data, error = null).apply {
                delegate = AudioPlayerDelegate(
                    onCompletion = {
                        _isPlaying.value = false
                        completionListener?.invoke()
                    }
                ).also { this@IOSQuranAudioPlayer.delegate = it }
                prepareToPlay()
                play()
            }
            _isPlaying.value = true
        } catch (e: Exception) {
            println("QuranAudioPlayer error: ${e.message}")
            _isPlaying.value = false
            _currentAyahNumber.value = -1
        }
    }

    override fun pause() {
        audioPlayer?.let {
            if (it.playing) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    override fun stop() {
        audioPlayer?.stop()
        audioPlayer = null
        _isPlaying.value = false
        _currentAyahNumber.value = -1
    }

    override fun release() {
        stop()
        completionListener = null
        delegate = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}

private class AudioPlayerDelegate(
    private val onCompletion: () -> Unit
) : NSObject(), AVAudioPlayerDelegateProtocol {
    override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
        onCompletion()
    }
}

actual fun createQuranAudioPlayer(): QuranAudioPlayer = IOSQuranAudioPlayer()
