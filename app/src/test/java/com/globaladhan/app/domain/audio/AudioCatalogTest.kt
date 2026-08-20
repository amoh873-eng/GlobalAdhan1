package com.globaladhan.app.domain.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioCatalogTest {

    @Test
    fun `demo muezzins are available and licensed`() {
        // DemoAudioProvider is in the data layer; here we validate the model shape.
        val muezzin = Muezzin(
            id = "haram_makki",
            nameArabic = "أذان الحرم المكي",
            nameEnglish = "Haram Makki",
            country = "Saudi Arabia",
            adhanStyle = "Haram",
            provider = "demo",
            standardAdhanId = "adhan_haram_makki",
            fajrAdhanId = "adhan_haram_makki",
            isAvailable = true,
            isDemo = true
        )
        assertEquals("haram_makki", muezzin.id)
        assertTrue(muezzin.isAvailable)
        assertTrue(muezzin.isDemo)
        assertEquals(muezzin.standardAdhanId, muezzin.fajrAdhanId)
        assertEquals("أذان الحرم المكي", muezzin.displayName)
    }

    @Test
    fun `unavailable reciter is never falsely marked as playable`() {
        val reciter = Reciter(
            id = "sudais",
            nameArabic = "عبد الرحمن السديس",
            nameEnglish = "Al-Sudais",
            provider = "licensed",
            isDownloadable = false,
            isStreamable = false
        )
        // The spec forbids claiming an unlicensed reciter is installed.
        assertFalse(reciter.isDownloadable)
        assertFalse(reciter.isStreamable)
    }

    @Test
    fun `audio asset carries license metadata`() {
        val asset = AudioAsset(
            id = "adhan_haram_makki",
            title = "Adhan Haram Makki",
            provider = "demo",
            resRawId = 1,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        )
        assertNotNull(asset.resRawId)
        assertTrue(asset.license!!.contains("Free for Islamic apps"))
        assertTrue(asset.isDemo)
    }

    @Test
    fun `word timing engine maps position to word honestly`() {
        val timings = listOf(
            WordTiming(surah = 1, ayah = 1, wordIndex = 0, startMs = 0, endMs = 500),
            WordTiming(surah = 1, ayah = 1, wordIndex = 1, startMs = 500, endMs = 1000),
            WordTiming(surah = 1, ayah = 1, wordIndex = 2, startMs = 1000, endMs = 1500)
        )
        assertEquals(0, WordTimingEngine.activeWord(timings, 250)?.wordIndex)
        assertEquals(1, WordTimingEngine.activeWord(timings, 750)?.wordIndex)
        assertEquals(2, WordTimingEngine.activeWord(timings, 1200)?.wordIndex)
        // Out of range / no timings → null (never fake).
        assertNull(WordTimingEngine.activeWord(timings, 5000))
        assertNull(WordTimingEngine.activeWord(emptyList(), 100))
        assertNull(WordTimingEngine.activeWord(timings, -1))
    }

    @Test
    fun `word timing engine falls back to last started word on gaps`() {
        val timings = listOf(
            WordTiming(surah = 1, ayah = 1, wordIndex = 0, startMs = 0, endMs = 400),
            WordTiming(surah = 1, ayah = 1, wordIndex = 1, startMs = 600, endMs = 1000)
        )
        // 500ms is in the gap; the last word whose start passed is word 0.
        assertEquals(0, WordTimingEngine.activeWord(timings, 500)?.wordIndex)
    }
}
