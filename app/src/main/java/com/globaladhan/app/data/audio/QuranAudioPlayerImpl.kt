package com.globaladhan.app.data.audio

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real Quran recitation player backed by Media3 (ExoPlayer).
 *
 * Loads audio files ONLY from the public Downloads folder:
 *
 *   /storage/emulated/0/Download/quran/{surah:03d}/{ayah:03d}.mp3
 *
 * It is strictly offline — no downloading, no streaming. If a file is missing
 * or unreadable, [loadError] carries a clear Arabic message ("ملف الآية غير
 * موجود") and playback does not start, so the UI can inform the user instead
 * of failing silently.
 */
@Singleton
class QuranAudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : com.globaladhan.app.domain.audio.QuranAudioPlayer {

    private val TAG = "QuranAudio"

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(exoListener)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: Boolean get() = _isPlaying.value

    private val _currentPositionMillis = MutableStateFlow(0L)
    override val currentPositionMillis: Long get() = _currentPositionMillis.value

    private val _totalDurationMillis = MutableStateFlow(0L)
    override val totalDurationMillis: Long get() = _totalDurationMillis.value

    private val _currentAyah = MutableStateFlow<Pair<Int, Int>?>(null) // surah, ayah
    val currentAyah: StateFlow<Pair<Int, Int>?> = _currentAyah.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    override val loadError: String? get() = _loadError.value

    private val _repeatMode =
        MutableStateFlow(com.globaladhan.app.domain.audio.RepeatMode.OFF)

    /**
     * Public root: /storage/emulated/0/Download/quran — where the user's MP3s
     * live (spec: {surah:03d}/{ayah:03d}.mp3 under Download/quran).
     */
    private val quranRoot: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "quran"
        )

    /** Spec layout: Download/quran/{surah:03d}/{ayah:03d}.mp3 */
    override fun audioFile(surahNumber: Int, ayahNumber: Int): File? {
        if (surahNumber <= 0 || ayahNumber <= 0) return null
        return File(
            File(quranRoot, "%03d".format(surahNumber)),
            "%03d.mp3".format(ayahNumber)
        )
    }

    override fun audioFileExists(surahNumber: Int, ayahNumber: Int): Boolean {
        val file = audioFile(surahNumber, ayahNumber) ?: return false
        return file.exists() && file.length() > 0
    }

    override suspend fun setRepeatMode(repeat: com.globaladhan.app.domain.audio.RepeatMode) {
        _repeatMode.value = repeat
    }

    /**
     * Play a local ayah file.
     * [surahNumber] and [ayahNumber] are required.
     */
    fun playLocal(
        file: File? = null,
        filePath: String? = null,
        resRawId: Int? = null,
        surahNumber: Int,
        ayahNumber: Int,
        ayahCount: Int,
        onAyahEnd: (() -> Unit)? = null
    ) {
        // 🔍 رسائل التصحيح - ستراها في Logcat
        val specFile = File("/storage/emulated/0/Download/quran/${String.format("%03d", surahNumber)}/${String.format("%03d", ayahNumber)}.mp3")
        Log.d(TAG, "========== بدء تشغيل ==========")
        Log.d(TAG, "السورة: $surahNumber, الآية: $ayahNumber")
        Log.d(TAG, "المسار الكامل: ${specFile.absolutePath}")
        Log.d(TAG, "الملف موجود؟ ${specFile.exists()}")
        Log.d(TAG, "حجم الملف: ${if (specFile.exists()) specFile.length() else 0} بايت")
        Log.d(TAG, "هل يمكن القراءة؟ ${if (specFile.exists()) specFile.canRead() else false}")
        Log.d(TAG, "=================================")

        stopInternal()
        _currentAyah.value = surahNumber to ayahNumber
        _loadError.value = null

        val uri: android.net.Uri = when {
            resRawId != null -> android.net.Uri.parse(
                "android.resource://${context.packageName}/$resRawId"
            )
            file != null -> android.net.Uri.fromFile(file)
            filePath != null -> android.net.Uri.fromFile(File(filePath))
            else -> {
                val specFile2 = audioFile(surahNumber, ayahNumber)
                if (specFile2 != null && specFile2.exists() && specFile2.length() > 0) {
                    Log.d(TAG, "✅ باستخدام الملف من audioFile(): ${specFile2.absolutePath}")
                    android.net.Uri.fromFile(specFile2)
                } else {
                    val errorMsg = "ملف الآية غير موجود: ${specFile.absolutePath}"
                    Log.e(TAG, "❌ $errorMsg")
                    _loadError.value = errorMsg
                    return
                }
            }
        }

        try {
            Log.d(TAG, "✅ URI: $uri")
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.play()
            _totalDurationMillis.value = 0L
            _isPlaying.value = true
            startTicker()
            Log.d(TAG, "✅ تم بدء التشغيل بنجاح")
        } catch (e: Exception) {
            val errorMsg = "فشل التشغيل: ${e.message}"
            Log.e(TAG, "❌ $errorMsg", e)
            _loadError.value = "ملف الآية غير موجود"
            stopInternal()
        }
    }

    /** Whether the current surah has more ayahs after [ayahNumber]. */
    private fun hasNextAyah(surahNumber: Int, ayahNumber: Int, ayahCount: Int): Boolean =
        ayahNumber < ayahCount && audioFileExists(surahNumber, ayahNumber + 1)

    private fun handleCompletion(surahNumber: Int, ayahNumber: Int, ayahCount: Int, onAyahEnd: (() -> Unit)?) {
        when (_repeatMode.value) {
            com.globaladhan.app.domain.audio.RepeatMode.AYAH -> {
                playLocal(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    ayahCount = ayahCount,
                    onAyahEnd = onAyahEnd
                )
            }
            com.globaladhan.app.domain.audio.RepeatMode.SURAH -> {
                if (hasNextAyah(surahNumber, ayahNumber, ayahCount)) {
                    playLocal(
                        surahNumber = surahNumber,
                        ayahNumber = ayahNumber + 1,
                        ayahCount = ayahCount,
                        onAyahEnd = onAyahEnd
                    )
                } else {
                    playLocal(
                        surahNumber = surahNumber,
                        ayahNumber = 1,
                        ayahCount = ayahCount,
                        onAyahEnd = onAyahEnd
                    )
                }
            }
            com.globaladhan.app.domain.audio.RepeatMode.OFF -> {
                onAyahEnd?.invoke()
                stopInternal()
            }
        }
    }

    override suspend fun playSurah(surahNumber: Int, startAyah: Int) {
        if (!audioFileExists(surahNumber, startAyah)) {
            _loadError.value = "ملف الآية غير موجود"
            return
        }
        playLocal(
            surahNumber = surahNumber,
            ayahNumber = startAyah,
            ayahCount = Int.MAX_VALUE,
            onAyahEnd = null
        )
    }

    override suspend fun playAyah(surahNumber: Int, ayahNumber: Int) {
        if (!audioFileExists(surahNumber, ayahNumber)) {
            _loadError.value = "ملف الآية غير موجود"
            return
        }
        playLocal(
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
            ayahCount = Int.MAX_VALUE,
            onAyahEnd = null
        )
    }

    override suspend fun pause() {
        runCatching { exoPlayer.pause() }
        _isPlaying.value = false
        tickerJob?.cancel()
    }

    override suspend fun resume() {
        runCatching { exoPlayer.play() }
        _isPlaying.value = true
        startTicker()
    }

    override suspend fun stop() = stopInternal()

    override suspend fun nextAyah() {
        val (s, a) = _currentAyah.value ?: return
        playLocal(surahNumber = s, ayahNumber = a + 1, ayahCount = Int.MAX_VALUE, onAyahEnd = null)
    }

    override suspend fun previousAyah() {
        val (s, a) = _currentAyah.value ?: return
        if (a > 1) playLocal(surahNumber = s, ayahNumber = a - 1, ayahCount = Int.MAX_VALUE, onAyahEnd = null)
    }

    fun seekTo(positionMs: Long) {
        runCatching { exoPlayer.seekTo(positionMs) }
        _currentPositionMillis.value = positionMs
    }

    private val exoListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) startTicker() else tickerJob?.cancel()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _currentAyah.value?.let { (s, a) ->
                    handleCompletion(s, a, Int.MAX_VALUE, null)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "❌ ExoPlayer error", error)
            _loadError.value = "ملف الآية غير موجود"
            stopInternal()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (_isPlaying.value) {
                _currentPositionMillis.value = exoPlayer.currentPosition
                if (_totalDurationMillis.value == 0L) {
                    _totalDurationMillis.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(100)
            }
        }
    }

    private fun stopInternal() {
        tickerJob?.cancel()
        tickerJob = null
        runCatching { exoPlayer.stop() }
        runCatching { exoPlayer.clearMediaItems() }
        _isPlaying.value = false
        _currentPositionMillis.value = 0L
    }
}