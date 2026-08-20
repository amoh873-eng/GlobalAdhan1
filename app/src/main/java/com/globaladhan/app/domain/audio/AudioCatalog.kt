package com.globaladhan.app.domain.audio

/**
 * Reciter catalog entry (spec §1). Loaded from a structured config source,
 * never hard-coded in UI components.
 */
data class Reciter(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val country: String? = null,
    val style: String? = null,
    val provider: String = "demo",
    val audioBaseUrl: String? = null,
    val availableSurahs: IntRange? = null,
    val isDownloadable: Boolean = false,
    val isStreamable: Boolean = false,
    val license: String? = null,
    val isDemo: Boolean = false
) {
    val displayName: String get() = nameArabic
}

/**
 * Muezzin (Adhan) catalog entry (spec §2).
 */
data class Muezzin(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val country: String? = null,
    val adhanStyle: String? = null,
    val provider: String = "demo",
    val standardAdhanId: String? = null,
    val fajrAdhanId: String? = null,
    val iqamahId: String? = null,
    val isAvailable: Boolean = false,
    val license: String? = null,
    val isDemo: Boolean = false
) {
    val displayName: String get() = nameArabic
}

/**
 * A single audio asset (adhan or ayah/surah recording).
 */
data class AudioAsset(
    val id: String,
    val title: String,
    val provider: String,
    val uri: String? = null,      // remote URL or content:// for user audio
    val resRawId: Int? = null,    // bundled res/raw for demo/local
    val fileFormat: String = "mp3",
    val quality: String = "128kbps",
    val sizeBytes: Long = 0L,
    val license: String? = null,
    val checksum: String? = null,
    val isDemo: Boolean = false
)

/** License/source metadata (spec §11). */
data class AudioSourceMetadata(
    val sourceName: String,
    val sourceUrl: String,
    val copyrightHolder: String,
    val license: String,
    val permissionStatus: String,     // e.g. "licensed", "demo", "user-provided"
    val attributionRequired: Boolean = false,
    val redistributionAllowed: Boolean = false
)
