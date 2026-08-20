package com.globaladhan.app.data.audio

import android.content.Context
import com.globaladhan.app.domain.audio.AudioAsset
import com.globaladhan.app.domain.audio.DownloadStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline audio download manager (spec §7). Tracks per-asset progress,
 * never downloads the same file twice, validates completion.
 */
@Singleton
class AudioDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: AudioStorageManager
) {

    data class DownloadTask(
        val assetId: String,
        val status: DownloadStatus,
        val progress: Int,      // 0..100
        val bytesDownloaded: Long,
        val totalBytes: Long
    )

    private val _tasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val tasks: StateFlow<Map<String, DownloadTask>> = _tasks.asStateFlow()

    private val activeDownloads = mutableSetOf<String>()

    suspend fun download(
        asset: AudioAsset,
        targetFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (activeDownloads.contains(asset.id)) return@withContext false
        if (storage.isDownloadComplete(targetFile)) {
            // Already downloaded — no duplicate work.
            updateTask(asset.id, DownloadStatus.DOWNLOADED, 100, targetFile.length(), targetFile.length())
            return@withContext true
        }

        activeDownloads.add(asset.id)
        try {
            val url = asset.uri ?: return@withContext false
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                setRequestProperty("Accept-Encoding", "identity")
            }
            conn.connect()

            val total = conn.contentLengthLong.coerceAtLeast(0L)
            val input = conn.inputStream
            targetFile.parentFile?.mkdirs()
            val tmp = File(targetFile.parentFile, targetFile.name + ".part")
            tmp.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                var read: Int
                var done = 0L
                while (input.read(buf).also { read = it } != -1) {
                    out.write(buf, 0, read)
                    done += read
                    val progress = if (total > 0) ((done * 100) / total).toInt() else 0
                    updateTask(asset.id, DownloadStatus.DOWNLOADING, progress, done, total)
                }
            }
            input.close()

            if (storage.isDownloadComplete(tmp)) {
                tmp.renameTo(targetFile)
                updateTask(asset.id, DownloadStatus.DOWNLOADED, 100, targetFile.length(), targetFile.length())
                true
            } else {
                tmp.delete()
                updateTask(asset.id, DownloadStatus.FAILED, 0, 0, total)
                false
            }
        } catch (e: Exception) {
            updateTask(asset.id, DownloadStatus.FAILED, 0, 0, 0)
            false
        } finally {
            activeDownloads.remove(asset.id)
        }
    }

    suspend fun deleteDownload(assetId: String) {
        _tasks.value = _tasks.value - assetId
    }

    fun statusOf(assetId: String): DownloadStatus =
        _tasks.value[assetId]?.status ?: DownloadStatus.NOT_DOWNLOADED

    private fun updateTask(
        assetId: String,
        status: DownloadStatus,
        progress: Int,
        done: Long,
        total: Long
    ) {
        _tasks.value = _tasks.value + (assetId to DownloadTask(assetId, status, progress, done, total))
    }
}
