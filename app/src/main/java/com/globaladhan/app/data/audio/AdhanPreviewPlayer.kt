package com.globaladhan.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Plays an Adhan recording for preview/testing WITHOUT scheduling any alarm
 * (spec §12, §16). Only one preview at a time; Play/Pause/Stop with progress.
 */
@Singleton
class AdhanPreviewPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var player: MediaPlayer? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    /** Currently previewed recording id, or null when stopped. */
    private val _currentRecordingId = MutableStateFlow<String?>(null)
    val currentRecordingId: StateFlow<String?> = _currentRecordingId.asStateFlow()

    fun play(resRawId: Int, recordingId: String) {
        stopInternal()
        _currentRecordingId.value = recordingId
        try {
            val afd = context.resources.openRawResourceFd(resRawId)
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                start()
                afd.close()
                _durationMs.value = duration.toLong()
            }
            _isPlaying.value = true
            _isPaused.value = false
            job = scope.launch {
                while (_isPlaying.value && player != null) {
                    _progressMs.value = player?.currentPosition?.toLong() ?: 0L
                    if (player != null && !player!!.isPlaying && !_isPaused.value) {
                        stopInternal()
                        break
                    }
                    delay(250)
                }
            }
        } catch (e: Exception) {
            stopInternal()
        }
    }

    fun pause() {
        player?.pause()
        _isPlaying.value = false
        _isPaused.value = true
    }

    fun resume() {
        player?.start()
        _isPlaying.value = true
        _isPaused.value = false
    }

    fun stop() {
        stopInternal()
    }

    private fun stopInternal() {
        job?.cancel()
        job = null
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        _isPlaying.value = false
        _isPaused.value = false
        _progressMs.value = 0L
    }
}
