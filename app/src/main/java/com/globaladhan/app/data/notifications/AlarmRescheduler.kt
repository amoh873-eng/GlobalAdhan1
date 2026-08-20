package com.globaladhan.app.data.notifications

import android.content.Context
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.repository.PrayerRepository
import com.globaladhan.app.domain.model.PrayerName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recomputes today's prayer times and reschedules all alarms.
 * Called on app start, boot, time-zone change, date change, and location change.
 */
@Singleton
class AlarmRescheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayerRepository: PrayerRepository,
    private val settings: SettingsRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun rescheduleAll() {
        scope.launch {
            val location = settings.savedLocation.first()
            val prayerSettings = settings.prayerSettings.first()
            if (!location.hasLocation) return@launch

            val day = prayerRepository.calculatePrayerDay(
                location = com.globaladhan.app.domain.model.GeoLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    country = location.country,
                    city = location.city,
                    timeZoneId = location.timeZoneId
                ),
                date = LocalDate.now()
            )

            val prayerTimes = mapOf(
                PrayerName.FAJR to day.fajr,
                PrayerName.DHUHR to day.dhuhr,
                PrayerName.ASR to day.asr,
                PrayerName.MAGHRIB to day.maghrib,
                PrayerName.ISHA to day.isha
            )

            PrayerAlarmScheduler.schedulePrayerAlarms(
                context = context,
                prayerTimes = prayerTimes,
                date = day.date,
                timeZoneId = location.timeZoneId
            )

            // Optional "prayer in N minutes" notification (spec §20).
            val adhan = settings.adhanSettings.first()
            val leadMinutes = adhan.notificationLeadMinutes
            if (leadMinutes > 0) {
                PrayerAlarmScheduler.scheduleLeadNotifications(
                    context = context,
                    prayerTimes = prayerTimes,
                    date = day.date,
                    timeZoneId = location.timeZoneId,
                    leadMinutes = leadMinutes
                )
            }
        }
    }
}
