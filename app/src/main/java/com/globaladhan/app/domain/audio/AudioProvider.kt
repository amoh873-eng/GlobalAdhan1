package com.globaladhan.app.domain.audio

/**
 * Audio provider abstraction (spec §3). The app never depends directly on a
 * concrete provider — implementations are swapped via configuration.
 *
 * Implementations: Local, Remote/Streaming, Licensed, Demo, User.
 */
interface AudioProvider {

    /** Unique provider key, e.g. "demo", "local", "licensed-user". */
    val key: String

    suspend fun getReciters(): List<Reciter>

    suspend fun getMuezzins(): List<Muezzin>

    /** Resolve the playable asset for a surah/ayah (returns null when unavailable). */
    suspend fun getSurahAudio(surah: Int, reciterId: String): AudioAsset?

    suspend fun getAyahAudio(surah: Int, ayah: Int, reciterId: String): AudioAsset?

    /** Resolve the adhan asset for a prayer (fajr vs standard). */
    suspend fun getAdhanAudio(muezzinId: String, isFajr: Boolean): AudioAsset?

    // Playback (suspend so providers can stream/buffer).
    suspend fun play(asset: AudioAsset)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()

    // Download management.
    suspend fun download(asset: AudioAsset): Boolean
    suspend fun deleteDownload(assetId: String)
    suspend fun getDownloadStatus(assetId: String): DownloadStatus
}

enum class DownloadStatus { NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, FAILED }
