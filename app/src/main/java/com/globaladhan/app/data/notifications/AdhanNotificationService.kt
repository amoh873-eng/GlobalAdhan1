package com.globaladhan.app.data.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.globaladhan.app.MainActivity
import com.globaladhan.app.R
import com.globaladhan.app.data.local.preferences.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shows the prayer notification and plays the Adhan sound (if configured).
 * Runs as a foreground service so playback continues even if the app is closed.
 */
@AndroidEntryPoint
class AdhanNotificationService : Service() {

    @Inject lateinit var settings: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.IO)
    private var player: MediaPlayer? = null
    private var playbackJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra(EXTRA_PRAYER) ?: return START_NOT_STICKY
        val short = intent.getBooleanExtra(EXTRA_SHORT, false)

        startForeground(NOTIFICATION_ID, buildNotification(prayer))

        val prayerEnum = com.globaladhan.app.domain.model.PrayerName.entries
            .firstOrNull { it.name == prayer }
        scope.launch {
            val adhanSettings = settings.adhanSettings.first()
            if (adhanSettings.enabled) {
                val reciterId = prayerEnum?.let { adhanSettings.selection.reciterIdFor(it) }
                    ?: adhanSettings.selection.defaultReciterId
                playAdhan(adhanSettings.volume, short, reciterId)
                if (adhanSettings.vibration) vibrate()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun playAdhan(volume: Float, short: Boolean, reciterId: String) {
        playbackJob = scope.launch {
            try {
                // Look up the selected recording from the licensed audio library.
                val audio = com.globaladhan.app.domain.audio.AdhanAudioLibrary.byId(reciterId)
                    ?: com.globaladhan.app.domain.audio.AdhanAudioLibrary.default()
                val resId = audio.resRawId ?: R.raw.adhan
                val afd = resources.openRawResourceFd(resId)
                player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setVolume(volume, volume)
                    prepare()
                    start()
                    afd.close()
                }
                // Let the full Adhan play; stop after it finishes.
                val duration = player?.duration?.toLong() ?: (audio.durationSeconds * 1000L)
                delay(if (short) duration.coerceAtMost(20_000) else duration)
                stopPlayback()
            } catch (e: Exception) {
                stopPlayback()
            }
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }

    private fun stopPlayback() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }

    private fun buildNotification(prayer: String): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, PrayerAlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$prayer ${getString(R.string.prayer_time_notification_text)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
    }

    override fun onDestroy() {
        playbackJob?.cancel()
        stopPlayback()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_SHORT = "extra_short"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context, prayer: String, short: Boolean = false) {
            val intent = Intent(context, AdhanNotificationService::class.java)
                .putExtra(EXTRA_PRAYER, prayer)
                .putExtra(EXTRA_SHORT, short)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
