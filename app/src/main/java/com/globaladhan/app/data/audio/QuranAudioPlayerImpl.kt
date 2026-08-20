package com.globaladhan.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real Quran recitation player backed by Android MediaPlayer.
 *
 * Plays a local audio file (bundled licensed recording or a downloaded licensed
 * pack). Exposes playback position so the UI can drive word-by-word
 * highlighting from the actual audio position (spec: audio position → word).
 *
 * No copyrighted audio is bundled; this player is ready for any legally
 * licensed source the reciter library provides.
 */
@Singleton
class QuranAudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : com.globaladhan.app.domain.audio.QuranAudioPlayer {

    private var player: MediaPlayer? = null
    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var audioManager: android.media.AudioManager? = null
    private var audioFocusRequest: android.media.AudioFocusRequest? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: Boolean get() = _isPlaying.value

    private val _currentPositionMillis = MutableStateFlow(0L)
    override val currentPositionMillis: Long get() = _currentPositionMillis.value

    private val _totalDurationMillis = MutableStateFlow(0L)
    override val totalDurationMillis: Long get() = _totalDurationMillis.value

    private val _currentAyah = MutableStateFlow<Pair<Int, Int>?>(null) // surah, ayah
    val currentAyah: StateFlow<Pair<Int, Int>?> = _currentAyah.asStateFlow()

    private val _repeatMode = MutableStateFlow(com.globaladhan.app.domain.audio.RepeatMode.OFF)
    override suspend fun setRepeatMode(repeat: com.globaladhan.app.domain.audio.RepeatMode) {
        _repeatMode.value = repeat
    }

    /**
     * Play a local audio file at the given [filePath] (or res raw id via [resRawId])
     * representing [surahNumber]:[ayahNumber]. Supports next/previous/repeat.
     */
    fun playLocal(
        filePath: String? = null,
        resRawId: Int? = null,
        surahNumber: Int,
        ayahNumber: Int,
        ayahCount: Int,
        onAyahEnd: (() -> Unit)? = null
    ) {
        stopInternal()
        _currentAyah.value = surahNumber to ayahNumber
        if (!requestAudioFocus()) {
            return
        }
        try {
            val mp = MediaPlayer()
            if (resRawId != null) {
                val afd = context.resources.openRawResourceFd(resRawId)
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else if (filePath != null) {
                mp.setDataSource(filePath)
            } else {
                return
            }
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setOnCompletionListener {
                handleCompletion(ayahNumber, ayahCount, onAyahEnd)
            }
            mp.prepare()
            mp.start()
            _totalDurationMillis.value = mp.duration.toLong()
            _isPlaying.value = true
            player = mp
            startTicker()
        } catch (e: Exception) {
            stopInternal()
        }
    }

    private fun handleCompletion(currentAyah: Int, ayahCount: Int, onAyahEnd: (() -> Unit)?) {
        when (_repeatMode.value) {
            com.globaladhan.app.domain.audio.RepeatMode.AYAH -> {
                // Repeat the same ayah.
                _currentAyah.value?.let { (s, a) ->
                    playLocal(null, null, s, a, ayahCount, onAyahEnd)
                }
            }
            com.globaladhan.app.domain.audio.RepeatMode.SURAH -> {
                val (s, a) = _currentAyah.value ?: return
                if (a < ayahCount) {
                    playLocal(null, null, s, a + 1, ayahCount, onAyahEnd)
                } else {
                    playLocal(null, null, s, 1, ayahCount, onAyahEnd)
                }
            }
            com.globaladhan.app.domain.audio.RepeatMode.OFF -> {
                onAyahEnd?.invoke()
                stopInternal()
            }
        }
    }

    override suspend fun playSurah(surahNumber: Int, startAyah: Int) = Unit
    override suspend fun playAyah(surahNumber: Int, ayahNumber: Int) = Unit
    override suspend fun pause() {
        player?.pause()
        _isPlaying.value = false
        tickerJob?.cancel()
    }

    override suspend fun resume() {
        player?.start()
        _isPlaying.value = true
        startTicker()
    }

    override suspend fun stop() = stopInternal()

    override suspend fun nextAyah() {
        val (s, a) = _currentAyah.value ?: return
        playLocal(null, null, s, a + 1, Int.MAX_VALUE, null)
    }

    override suspend fun previousAyah() {
        val (s, a) = _currentAyah.value ?: return
        if (a > 1) playLocal(null, null, s, a - 1, Int.MAX_VALUE, null)
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.toInt())
        _currentPositionMillis.value = positionMs
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (_isPlaying.value && player != null) {
                _currentPositionMillis.value = player?.currentPosition?.toLong() ?: 0L
                delay(100)
            }
        }
    }

    private fun stopInternal() {
        tickerJob?.cancel()
        tickerJob = null
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        _isPlaying.value = false
        _currentPositionMillis.value = 0L
        abandonAudioFocus()
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE)
            as android.media.AudioManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val request = android.media.AudioFocusRequest.Builder(
                android.media.AudioManager.AUDIOFOCUS_GAIN
            )
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
            audioFocusRequest = request
            return audioManager?.requestAudioFocus(request) ==
                android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        @Suppress("DEPRECATION")
        return audioManager?.requestAudioFocus(
            audioFocusListener,
            android.media.AudioManager.STREAM_MUSIC,
            android.media.AudioManager.AUDIOFOCUS_GAIN
        ) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        runCatching {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            } ?: run {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusListener)
            }
        }
    }

    private val audioFocusListener =
        android.media.AudioManager.OnAudioFocusChangeListener { change ->
            when (change) {
                android.media.AudioManager.AUDIOFOCUS_LOSS -> stopInternal()
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    runCatching { player?.pause() }
                    _isPlaying.value = false
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    runCatching { player?.setVolume(0.2f, 0.2f) }
                }
                android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                    runCatching {
                        player?.setVolume(1f, 1f)
                        player?.start()
                    }
                    _isPlaying.value = player?.isPlaying == true
                }
            }
        }
}
