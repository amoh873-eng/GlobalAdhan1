package com.globaladhan.app.data.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.globaladhan.app.domain.model.PrayerName
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Schedules exact alarms for each prayer time using AlarmManager.
 * Alarms survive app closure; a BootReceiver restores them after reboot.
 */
object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"

    const val EXTRA_PRAYER = "extra_prayer"
    const val EXTRA_TIMESTAMP = "extra_timestamp"
    const val EXTRA_LEAD_MINUTES = "extra_lead_minutes"
    const val LEAD_BASE_REQUEST = 1000

    fun schedulePrayerAlarms(
        context: Context,
        prayerTimes: Map<PrayerName, LocalTime>,
        date: LocalDate,
        timeZoneId: String = java.util.TimeZone.getDefault().id
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        // Cancel previous alarms for these prayers to avoid duplicates
        PrayerName.entries.forEach { prayer ->
            cancelAlarm(context, alarmManager, prayer)
        }

        // Use the saved location's IANA time zone, never a fixed offset.
        val zone = runCatching { ZoneId.of(timeZoneId) }
            .getOrDefault(ZoneId.systemDefault())

        // Schedule prayers that are still ahead today. Also schedule a follow-up
        // so that tomorrow's Fajr is covered even if the app is never reopened:
        // the day's first prayer (Fajr) is scheduled on the following day when it
        // is the next upcoming event.
        prayerTimes.forEach { (prayer, time) ->
            val triggerAt = ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
            if (triggerAt > System.currentTimeMillis()) {
                scheduleAlarm(context, alarmManager, prayer, triggerAt)
            }
        }

        // Tomorrow's Fajr (first prayer of the next day) as a safety net, only
        // when today's Fajr has already passed so the two never collide.
        val todayFajr = prayerTimes[PrayerName.FAJR]
        val todayFajrTrigger = todayFajr?.let {
            ZonedDateTime.of(date, it, zone).toInstant().toEpochMilli()
        }
        if (todayFajrTrigger != null && todayFajrTrigger < System.currentTimeMillis()) {
            val tomorrowTrigger = ZonedDateTime.of(date.plusDays(1), todayFajr, zone)
                .toInstant().toEpochMilli()
            scheduleAlarm(context, alarmManager, PrayerName.FAJR, tomorrowTrigger)
        }
    }

    /**
     * Schedule "prayer in N minutes" reminder notifications (spec §20).
     * Uses a separate request-code range so they don't collide with the Adhan alarms.
     */
    fun scheduleLeadNotifications(
        context: Context,
        prayerTimes: Map<PrayerName, LocalTime>,
        date: LocalDate,
        timeZoneId: String,
        leadMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zone = runCatching { ZoneId.of(timeZoneId) }
            .getOrDefault(ZoneId.systemDefault())

        prayerTimes.forEach { (prayer, time) ->
            val triggerAt = ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
            val leadAt = triggerAt - leadMinutes * 60_000L
            if (leadAt > System.currentTimeMillis() && triggerAt > System.currentTimeMillis()) {
                val intent = Intent(context, PrayerLeadReceiver::class.java)
                    .putExtra(EXTRA_PRAYER, prayer.name)
                    .putExtra(EXTRA_LEAD_MINUTES, leadMinutes)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    LEAD_BASE_REQUEST + prayer.ordinal,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP, leadAt, pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, leadAt, pendingIntent)
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, leadAt, pendingIntent)
                    }
                    Log.d(TAG, "Scheduled lead notification for $prayer at $leadAt")
                } catch (e: SecurityException) {
                    Log.e(TAG, "Failed to schedule lead for $prayer", e)
                }
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayer: PrayerName,
        triggerAt: Long
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
            .putExtra(EXTRA_PRAYER, prayer.name)
            .putExtra(EXTRA_TIMESTAMP, triggerAt)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayer.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact to avoid crashing without permission
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            Log.d(TAG, "Scheduled $prayer at $triggerAt")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule $prayer", e)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, prayer: PrayerName) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
            .putExtra(EXTRA_PRAYER, prayer.name)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayer.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun createChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Times",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan and prayer time notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    const val CHANNEL_ID = "prayer_alerts"
}

/** Restores alarms after reboot, time-zone change, or app update. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRescheduler: AlarmRescheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_DATE_CHANGED
        ) {
            alarmRescheduler.rescheduleAll()
        }
    }
}

/** Shows the "prayer in N minutes" reminder notification (spec §20). */
@AndroidEntryPoint
class PrayerLeadReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationDispatcher: AdhanNotificationDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER) ?: return
        val leadMinutes = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_LEAD_MINUTES, 10)
        notificationDispatcher.showLeadNotification(context, prayer, leadMinutes)
    }
}
