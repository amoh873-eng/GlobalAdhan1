package com.globaladhan.app.domain.audio

/**
 * Abstraction for Quran recitation playback.
 *
 * The app deliberately does NOT bundle copyrighted recitations. This interface
 * defines the contract a licensed audio backend (e.g. a download-on-demand
 * provider or a locally imported recitation pack) must implement.
 *
 * A future implementation can be injected via Hilt without changing the UI.
 */
interface QuranAudioPlayer {
    suspend fun playSurah(surahNumber: Int, startAyah: Int)
    suspend fun playAyah(surahNumber: Int, ayahNumber: Int)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun nextAyah()
    suspend fun previousAyah()
    suspend fun setRepeatMode(repeat: RepeatMode)
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
    override val isPlaying: Boolean = false
    override val currentPositionMillis: Long = 0L
    override val totalDurationMillis: Long = 0L
}
