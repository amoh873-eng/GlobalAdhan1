package com.globaladhan.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.globaladhan.app.R
import com.globaladhan.app.domain.model.PrayerName

/**
 * Returns the localized prayer name (Arabic names when Arabic is the app
 * locale, per spec §3). Prayer labels must never appear in English while
 * Arabic mode is active.
 */
@Composable
fun localizedPrayerName(prayer: PrayerName): String = when (prayer) {
    PrayerName.FAJR -> stringResource(R.string.prayer_name_fajr)
    PrayerName.SUNRISE -> stringResource(R.string.prayer_name_sunrise)
    PrayerName.DHUHR -> stringResource(R.string.prayer_name_dhuhr)
    PrayerName.ASR -> stringResource(R.string.prayer_name_asr)
    PrayerName.MAGHRIB -> stringResource(R.string.prayer_name_maghrib)
    PrayerName.ISHA -> stringResource(R.string.prayer_name_isha)
    PrayerName.MIDNIGHT -> stringResource(R.string.midnight)
}
