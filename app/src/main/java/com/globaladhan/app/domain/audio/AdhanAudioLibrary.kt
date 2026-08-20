package com.globaladhan.app.domain.audio

import com.globaladhan.app.domain.model.PrayerName

/**
 * Metadata for a licensed Adhan recording.
 *
 * Every bundled or downloadable recording must carry its license info so the
 * app can verify redistribution rights (spec §17). No copyrighted recordings
 * are bundled without explicit permission.
 */
data class AdhanAudio(
    val id: String,
    val reciterName: String,
    val recordingName: String,
    val license: String,
    val source: String,
    val resRawId: Int? = null,   // bundled res/raw resource id (0 when not bundled)
    val durationSeconds: Int
)

/** The user's per-prayer Adhan selection. */
data class AdhanSelection(
    val defaultReciterId: String,
    val perPrayer: Map<PrayerName, String>
) {
    fun reciterIdFor(prayer: PrayerName): String =
        perPrayer[prayer] ?: defaultReciterId
}

/**
 * The Adhan audio library: the catalog of legally usable recordings.
 *
 * Currently bundles one licensed recording (Haram Makki by Ali Ibn Ahmad Mala,
 * free for Islamic apps). The architecture supports adding licensed packs
 * (bundled or downloadable) without changing the UI.
 */
object AdhanAudioLibrary {

    val recordings: List<AdhanAudio> = listOf(
        AdhanAudio(
            id = "haram_makki",
            reciterName = "Ali Ibn Ahmad Mala",
            recordingName = "Adhan Al-Haram Al-Maki (Mecca)",
            license = "Free for Islamic apps / personal use (Kiwifu/adhan-mp3)",
            source = "https://github.com/Kiwifu/adhan-mp3",
            resRawId = com.globaladhan.app.R.raw.adhan,
            durationSeconds = 90
        ),
        AdhanAudio(
            id = "haram_madani",
            reciterName = "Masjid an-Nabawi",
            recordingName = "Adhan Al-Haram Al-Madani (Madinah)",
            license = "Free for Islamic apps / personal use (Kiwifu/adhan-mp3)",
            source = "https://github.com/Kiwifu/adhan-mp3",
            resRawId = com.globaladhan.app.R.raw.adhan_madina,
            durationSeconds = 75
        ),
        AdhanAudio(
            id = "dubai",
            reciterName = "Dubai (UAE)",
            recordingName = "Adhan Dubai UAE",
            license = "Free for Islamic apps / personal use (Kiwifu/adhan-mp3)",
            source = "https://github.com/Kiwifu/adhan-mp3",
            resRawId = com.globaladhan.app.R.raw.adhan_dubai,
            durationSeconds = 80
        )
    )

    fun byId(id: String): AdhanAudio? = recordings.firstOrNull { it.id == id }

    fun default(): AdhanAudio = recordings.first()
}
