package com.globaladhan.app.data.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Storage Manager (spec §8): organized directories, storage accounting,
 * download validation, and deletion.
 *
 * Layout:
 *   /audio/quran/<reciter_id>/<surah:03d>/<ayah:03d>.mp3
 *   /audio/adhan/<muezzin_id>/fajr.mp3 | standard.mp3
 */
@Singleton
class AudioStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val audioRoot: File
        get() = File(context.filesDir, "audio").apply { mkdirs() }

    val quranDir: File get() = File(audioRoot, "quran").apply { mkdirs() }
    val adhanDir: File get() = File(audioRoot, "adhan").apply { mkdirs() }

    fun quranAyahFile(reciterId: String, surah: Int, ayah: Int): File =
        File(File(File(quranDir, reciterId), "%03d".format(surah)), "%03d.mp3".format(ayah))

    fun adhanFile(muezzinId: String, isFajr: Boolean): File =
        File(File(adhanDir, muezzinId), if (isFajr) "fajr.mp3" else "standard.mp3")

    /** Bytes used by downloaded audio. */
    fun storageUsedBytes(): Long =
        quranDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } +
            adhanDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun formatBytes(bytes: Long): String {
        if (bytes >= 1 shl 20) return "%.1f MB".format(bytes / (1 shl 20).toDouble())
        if (bytes >= 1 shl 10) return "%.1f KB".format(bytes / (1 shl 10).toDouble())
        return "$bytes B"
    }

    /** A download is complete only if the file exists and is non-empty. */
    fun isDownloadComplete(file: File): Boolean = file.exists() && file.length() > 0

    fun delete(file: File) {
        runCatching { file.delete() }
    }

    fun deleteReciterAudio(reciterId: String) {
        runCatching { File(quranDir, reciterId).deleteRecursively() }
    }

    fun deleteMuezzinAudio(muezzinId: String) {
        runCatching { File(adhanDir, muezzinId).deleteRecursively() }
    }

    fun clearCache() {
        runCatching { File(context.cacheDir, "audio").deleteRecursively() }
    }
}
