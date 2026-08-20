package com.globaladhan.app.data.audio

import com.globaladhan.app.domain.audio.AudioAsset
import com.globaladhan.app.domain.audio.AudioProvider
import com.globaladhan.app.domain.audio.DownloadStatus
import com.globaladhan.app.domain.audio.Muezzin
import com.globaladhan.app.domain.audio.Reciter

/**
 * DemoAudioProvider (spec §20): lets the whole audio system be tested without
 * production licensing. Uses ONLY legally available assets:
 *   - Adhan: the bundled licensed recordings (free for Islamic apps,
 *     Kiwifu/adhan-mp3) — real audio, clearly labeled DEMO.
 *   - Quran: no bundled recitation exists yet; assets are listed as unavailable
 *     rather than faking playback.
 *
 * The production provider replaces this without touching the Quran/prayer UI.
 */
class DemoAudioProvider(
    private val player: com.globaladhan.app.data.audio.QuranAudioPlayerImpl
) : AudioProvider {

    override val key = "demo"

    // Bundled licensed Adhan recordings (res/raw).
    private val demoAdhanAssets = mapOf(
        "haram_makki" to AudioAsset(
            id = "adhan_haram_makki",
            title = "Adhan Al-Haram Al-Maki (Demo)",
            provider = key,
            resRawId = com.globaladhan.app.R.raw.adhan,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        ),
        "haram_madani" to AudioAsset(
            id = "adhan_haram_madani",
            title = "Adhan Al-Haram Al-Madani (Demo)",
            provider = key,
            resRawId = com.globaladhan.app.R.raw.adhan_madina,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        ),
        "dubai" to AudioAsset(
            id = "adhan_dubai",
            title = "Adhan Dubai (Demo)",
            provider = key,
            resRawId = com.globaladhan.app.R.raw.adhan_dubai,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        )
    )

    private val demoMuezzins = listOf(
        Muezzin(
            id = "haram_makki",
            nameArabic = "أذان الحرم المكي",
            nameEnglish = "Haram Makki",
            country = "Saudi Arabia",
            adhanStyle = "Haram",
            provider = key,
            standardAdhanId = "adhan_haram_makki",
            fajrAdhanId = "adhan_haram_makki",
            isAvailable = true,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        ),
        Muezzin(
            id = "haram_madani",
            nameArabic = "أذان الحرم المدني",
            nameEnglish = "Haram Madani",
            country = "Saudi Arabia",
            adhanStyle = "Haram",
            provider = key,
            standardAdhanId = "adhan_haram_madani",
            fajrAdhanId = "adhan_haram_madani",
            isAvailable = true,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        ),
        Muezzin(
            id = "dubai",
            nameArabic = "أذان دبي",
            nameEnglish = "Dubai",
            country = "UAE",
            adhanStyle = "Gulf",
            provider = key,
            standardAdhanId = "adhan_dubai",
            fajrAdhanId = "adhan_dubai",
            isAvailable = true,
            license = "Free for Islamic apps (Kiwifu/adhan-mp3)",
            isDemo = true
        )
    )

    /** No Quran recitation bundled yet — listed honestly as unavailable. */
    private val demoReciters = listOf(
        Reciter(
            id = "demo_none",
            nameArabic = "لا يوجد قارئ بعد (Demo)",
            nameEnglish = "No reciter yet (Demo)",
            provider = key,
            isDownloadable = false,
            isStreamable = false,
            license = "Unavailable until a licensed source is configured",
            isDemo = true
        )
    )

    override suspend fun getReciters(): List<Reciter> = demoReciters

    override suspend fun getMuezzins(): List<Muezzin> = demoMuezzins

    override suspend fun getSurahAudio(surah: Int, reciterId: String): AudioAsset? = null

    override suspend fun getAyahAudio(surah: Int, ayah: Int, reciterId: String): AudioAsset? = null

    override suspend fun getAdhanAudio(muezzinId: String, isFajr: Boolean): AudioAsset? {
        val muezzin = demoMuezzins.firstOrNull { it.id == muezzinId } ?: return null
        val assetId = if (isFajr) muezzin.fajrAdhanId else muezzin.standardAdhanId
        return demoAdhanAssets[assetId]
    }

    override suspend fun play(asset: AudioAsset) {
        if (asset.resRawId != null) {
            player.playLocal(
                resRawId = asset.resRawId,
                surahNumber = 0,
                ayahNumber = 0,
                ayahCount = 1
            )
        }
    }

    override suspend fun pause() = player.pause()
    override suspend fun resume() = player.resume()
    override suspend fun stop() = player.stop()

    override suspend fun download(asset: AudioAsset): Boolean {
        // Demo assets are bundled — already "downloaded".
        return asset.resRawId != null
    }

    override suspend fun deleteDownload(assetId: String) = Unit

    override suspend fun getDownloadStatus(assetId: String): DownloadStatus =
        if (demoAdhanAssets.containsKey(assetId)) DownloadStatus.DOWNLOADED
        else DownloadStatus.NOT_DOWNLOADED
}
