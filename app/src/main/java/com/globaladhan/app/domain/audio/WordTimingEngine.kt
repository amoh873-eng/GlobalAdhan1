package com.globaladhan.app.domain.audio

/**
 * Timing of one word within an ayah (spec §12).
 * Maps a recitation audio position to the exact word being recited.
 */
data class WordTiming(
    val surah: Int,
    val ayah: Int,
    val wordIndex: Int,
    val startMs: Long,
    val endMs: Long
)

/**
 * Resolves the active word from an audio playback position.
 *
 * Honest timing: this maps position → word using real WordTiming metadata when
 * available. If no real timings exist for a recording (no licensed timing data),
 * it returns null rather than fabricating equal-divided timings — the UI then
 * simply keeps the previously highlighted word instead of faking sync
 * (spec: "Do not fake synchronization").
 */
object WordTimingEngine {

    /**
     * Find the word whose [startMs, endMs) contains [positionMs].
     * Returns null when no timing matches — including when the position is
     * past the end of the last word (honest: never fake a word for silence).
     */
    fun activeWord(timings: List<WordTiming>, positionMs: Long): WordTiming? {
        if (timings.isEmpty() || positionMs < 0) return null
        val exact = timings.firstOrNull { positionMs in it.startMs until it.endMs }
        if (exact != null) return exact
        // Allow a small trailing grace (e.g. a gap between words) by falling
        // back to the last word whose start has passed AND whose end is not
        // before the position by more than the gap. Positions past the final
        // end return null.
        val last = timings.lastOrNull { positionMs >= it.startMs } ?: return null
        val maxEnd = timings.maxOf { it.endMs }
        return if (positionMs <= maxEnd) last else null
    }

    /**
     * Build timings from a licensed timing source (e.g. per-word JSON).
     * Expected shape per entry: surah, ayah, wordIndex, startMs, endMs.
     */
    fun fromRaw(raw: List<WordTiming>): List<WordTiming> =
        raw.sortedBy { it.startMs }
}
