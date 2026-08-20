package com.globaladhan.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.notifications.AlarmRescheduler
import com.globaladhan.app.data.repository.PrayerRepository
import com.globaladhan.app.domain.audio.AdhanAudioLibrary
import com.globaladhan.app.domain.calendar.IslamicCalendar
import com.globaladhan.app.domain.model.IslamicDate
import com.globaladhan.app.domain.model.PrayerDay
import com.globaladhan.app.domain.model.PrayerTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

data class HomeUiState(
    val prayerDay: PrayerDay? = null,
    val location: SettingsRepository.StoredLocation? = null,
    val hijriDate: IslamicDate? = null,
    val adhanEnabled: Boolean = true,
    val muezzinName: String = "",
    val backgroundKey: String = "makkah",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val alarmRescheduler: AlarmRescheduler,
    private val settings: SettingsRepository,
    islamicCalendar: IslamicCalendar
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        prayerRepository.prayerDayFlow(),
        prayerRepository.locationFlow(),
        settings.adhanSettings,
        settings.background
    ) { day, location, adhan, background ->
        HomeUiState(
            prayerDay = day,
            location = location,
            hijriDate = islamicCalendar.toHijri(java.time.LocalDate.now()),
            adhanEnabled = adhan.enabled,
            muezzinName = AdhanAudioLibrary.byId(adhan.selection.defaultReciterId)?.reciterName ?: "",
            backgroundKey = background,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun refreshLocation() {
        viewModelScope.launch {
            val success = prayerRepository.refreshLocation()
            if (success) alarmRescheduler.rescheduleAll()
        }
    }

    companion object {
        /** Find the next upcoming prayer among today's times. */
        fun nextPrayer(day: PrayerDay, now: LocalTime = LocalTime.now()): PrayerTime? {
            val today = day.times()
                .filter { it.name != com.globaladhan.app.domain.model.PrayerName.MIDNIGHT }
                .sortedBy { it.time.toSecondOfDay() }

            val upcoming = today.firstOrNull { it.time.isAfter(now) }
            return upcoming ?: today.firstOrNull() // wraps to tomorrow's Fajr
        }

        /** Countdown (HH:MM:SS) to the next prayer. */
        fun countdownTo(next: PrayerTime, now: LocalTime = LocalTime.now()): String {
            val target = next.time
            val seconds = if (target.isAfter(now)) {
                Duration.between(now, target).seconds
            } else {
                // Next day
                Duration.between(now, target).seconds + 86_400
            }
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return "%02d:%02d:%02d".format(h, m, s)
        }

        /** Current prayer (the most recent one that has started). */
        fun currentPrayer(day: PrayerDay, now: LocalTime = LocalTime.now()): PrayerTime? {
            return day.times()
                .filter { it.name != com.globaladhan.app.domain.model.PrayerName.MIDNIGHT }
                .sortedBy { it.time.toSecondOfDay() }
                .lastOrNull { !it.time.isAfter(now) }
        }
    }
}
