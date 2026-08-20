package com.globaladhan.app.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives exact-alarm broadcasts for each prayer and shows the notification.
 */
@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationDispatcher: AdhanNotificationDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER)
        if (prayer == null) return

        notificationDispatcher.showPrayerNotification(context, prayer)
        Timber.d("Prayer alarm fired: $prayer")
    }
}
