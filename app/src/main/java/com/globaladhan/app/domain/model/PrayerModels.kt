package com.globaladhan.app.domain.model

import java.time.LocalDate

/**
 * A geographic location used for prayer time and Qibla calculations.
 * Kept as a plain value type so it can be stored and passed around freely.
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val city: String? = null,
    val region: String? = null,
    val timeZoneId: String = java.util.TimeZone.getDefault().id
) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0
}

enum class PrayerName(val arabicName: String) {
    FAJR("الفجر"),
    SUNRISE("الشروق"),
    DHUHR("الظهر"),
    ASR("العصر"),
    MAGHRIB("المغرب"),
    ISHA("العشاء"),
    MIDNIGHT("منتصف الليل")
}

data class PrayerTime(
    val name: PrayerName,
    val time: java.time.LocalTime,
    val isNext: Boolean = false
)

data class PrayerDay(
    val date: LocalDate,
    val location: GeoLocation,
    val fajr: java.time.LocalTime,
    val sunrise: java.time.LocalTime,
    val dhuhr: java.time.LocalTime,
    val asr: java.time.LocalTime,
    val maghrib: java.time.LocalTime,
    val isha: java.time.LocalTime,
    val midnight: java.time.LocalTime,
    val calculationMethod: CalculationMethod,
    val asrMethod: AsrMethod,
    val adjustments: Map<PrayerName, Int> = emptyMap()
) {
    fun times(): List<PrayerTime> = listOf(
        PrayerTime(PrayerName.FAJR, fajr),
        PrayerTime(PrayerName.SUNRISE, sunrise),
        PrayerTime(PrayerName.DHUHR, dhuhr),
        PrayerTime(PrayerName.ASR, asr),
        PrayerTime(PrayerName.MAGHRIB, maghrib),
        PrayerTime(PrayerName.ISHA, isha),
        PrayerTime(PrayerName.MIDNIGHT, midnight)
    )
}
