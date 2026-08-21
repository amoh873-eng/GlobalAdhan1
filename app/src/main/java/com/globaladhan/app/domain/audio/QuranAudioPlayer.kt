package com.globaladhan.app.domain.audio

import java.io.File

/**
 * Abstraction for Quran recitation playback.
 *
 * The app deliberately does NOT bundle copyrighted recitations. Recitation MP3s
 * are expected to exist on the device's public Downloads folder at:
 *
 *   /storage/emulated/0/Download/quran/{surah:03d}/{ayah:03d}.mp3
 *
 * The player is strictly OFFLINE — it never downloads or streams from the
 * internet. If a file is missing, [loadError] is set to a clear user-facing
 * message and playback does not start.
 */
interface QuranAudioPlayer {

    /** Start playing from [startAyah] of [surahNumber] to the end of the surah. */
    suspend fun playSurah(surahNumber: Int, startAyah: Int)

    /** Start playing the single ayah [ayahNumber] of [surahNumber]. */
    suspend fun playAyah(surahNumber: Int, ayahNumber: Int)

    suspend fun pause()
    suspend fun resume()
    suspend fun stop()

    /** Advance to the next ayah of the current surah. */
    suspend fun nextAyah()

    /** Go back to the previous ayah of the current surah. */
    suspend fun previousAyah()

    suspend fun setRepeatMode(repeat: RepeatMode)

    /** True if a file exists for [surahNumber]:[ayahNumber] at the spec path. */
    fun audioFileExists(surahNumber: Int, ayahNumber: Int): Boolean

    /** Resolve the local MP3 file for [surahNumber]:[ayahNumber], or null. */
    fun audioFile(surahNumber: Int, ayahNumber: Int): File?

    /**
     * Set while the last playback attempt failed because the audio file is
     * missing/unreadable. UI shows this to the user (e.g. "ملف الآية غير موجود").
     */
    val loadError: String?

    val isPlaying: Boolean
    val currentPositionMillis: Long
    val totalDurationMillis: Long
}

enum class RepeatMode { OFF, AYAH, SURAH }

/**
 * Default no-op implementation used until a licensed recitation source is
 * configured. Keeps the UI functional and the app free of copyrighted audio.
 */
class NoOpQuranAudioPlayer : QuranAudioPlayer {
    override suspend fun playSurah(surahNumber: Int, startAyah: Int) = Unit
    override suspend fun playAyah(surahNumber: Int, ayahNumber: Int) = Unit
    override suspend fun pause() = Unit
    override suspend fun resume() = Unit
    override suspend fun stop() = Unit
    override suspend fun nextAyah() = Unit
    override suspend fun previousAyah() = Unit
    override suspend fun setRepeatMode(repeat: RepeatMode) = Unit
    override fun audioFileExists(surahNumber: Int, ayahNumber: Int): Boolean = false
    override fun audioFile(surahNumber: Int, ayahNumber: Int): File? = null
    override val loadError: String? = null
    override val isPlaying: Boolean = false
    override val currentPositionMillis: Long = 0L
    override val totalDurationMillis: Long = 0L
}
