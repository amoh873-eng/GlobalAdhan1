package com.globaladhan.app.data.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.domain.model.PrayerName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and shows the prayer-time notification, and starts the Adhan
 * playback service so the call-to-prayer plays even when the app is closed.
 * Respects the per-prayer alert mode (full/short/notification-only/silent).
 */
@Singleton
class AdhanNotificationDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun showPrayerNotification(context: Context, prayer: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val prayerEnum = PrayerName.entries.firstOrNull { it.name == prayer } ?: return

        scope.launch {
            val adhan = settings.adhanSettings.first()
            if (!adhan.enabled) return@launch

            val config = adhan.perPrayerConfig[prayerEnum] ?: return@launch
            if (!config.enabled) return@launch

            when (config.mode) {
                SettingsRepository.AlertMode.SILENT -> return@launch
                SettingsRepository.AlertMode.NOTIFICATION_ONLY -> {
                    // Show notification without Adhan sound
                    showSilentNotification(context, prayerEnum)
                }
                SettingsRepository.AlertMode.FULL_ADHAN -> {
                    AdhanNotificationService.start(context, prayer, short = false)
                }
                SettingsRepository.AlertMode.SHORT_ADHAN -> {
                    AdhanNotificationService.start(context, prayer, short = true)
                }
            }
        }
    }

    private fun showSilentNotification(context: Context, prayer: PrayerName) {
        val notification = androidx.core.app.NotificationCompat.Builder(
            context, PrayerAlarmScheduler.CHANNEL_ID
        )
            .setSmallIcon(com.globaladhan.app.R.drawable.ic_notification)
            .setContentTitle(context.getString(com.globaladhan.app.R.string.app_name))
            .setContentText(
                "${prayer.name.replaceFirstChar { it.uppercase() }} " +
                    context.getString(com.globaladhan.app.R.string.prayer_time_notification_text)
            )
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        runCatching {
            androidx.core.app.NotificationManagerCompat.from(context)
                .notify(AdhanNotificationDispatcher.NOTIFICATION_ID, notification)
        }
    }

    /** "Maghrib prayer in 10 minutes" style reminder (spec §20). */
    fun showLeadNotification(context: Context, prayer: String, leadMinutes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = androidx.core.app.NotificationCompat.Builder(
            context, PrayerAlarmScheduler.CHANNEL_ID
        )
            .setSmallIcon(com.globaladhan.app.R.drawable.ic_notification)
            .setContentTitle(context.getString(com.globaladhan.app.R.string.app_name))
            .setContentText(
                context.getString(
                    com.globaladhan.app.R.string.prayer_in_minutes,
                    prayer.replaceFirstChar { it.uppercase() },
                    leadMinutes
                )
            )
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        runCatching {
            androidx.core.app.NotificationManagerCompat.from(context)
                .notify(AdhanNotificationDispatcher.LEAD_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1002
        const val LEAD_NOTIFICATION_ID = 1003
    }
}
