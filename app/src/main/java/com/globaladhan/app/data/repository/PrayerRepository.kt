package com.globaladhan.app.data.repository

import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.location.LocationProvider
import com.globaladhan.app.domain.calendar.IslamicCalendar
import com.globaladhan.app.domain.model.GeoLocation
import com.globaladhan.app.domain.model.PrayerDay
import com.globaladhan.app.domain.prayer.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerRepository @Inject constructor(
    private val calculator: PrayerTimeCalculator,
    private val settings: SettingsRepository,
    private val locationProvider: LocationProvider,
    private val islamicCalendar: IslamicCalendar
) {

    /** Current location flow (cached/manual selection). */
    fun locationFlow(): Flow<SettingsRepository.StoredLocation> = settings.savedLocation

    /** Combined flow: location + prayer settings → today's prayer day. */
    fun prayerDayFlow(): Flow<PrayerDay?> {
        return combine(settings.savedLocation, settings.prayerSettings) { location, prayerSettings ->
            if (!location.hasLocation) return@combine null
            val geo = GeoLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                country = location.country,
                city = location.city,
                timeZoneId = location.timeZoneId
            )
            calculator.calculate(
                location = geo,
                date = LocalDate.now(),
                method = prayerSettings.method,
                asrMethod = prayerSettings.asrMethod,
                highLatitudeMethod = prayerSettings.highLatitude,
                fajrAngleOverride = prayerSettings.fajrAngle.toDouble(),
                ishaAngleOverride = prayerSettings.ishaAngle.toDouble(),
                adjustments = prayerSettings.adjustmentMap()
            )
        }
    }

    /** Calculate prayer times for an arbitrary date (calendar view). */
    fun calculateForDate(
        location: GeoLocation,
        date: LocalDate
    ): PrayerDay? {
        return runCatching {
            calculator.calculate(location, date)
        }.getOrNull()
    }

    /**
     * Calculate prayer times for a single day, applying the user's saved
     * calculation profile (angles + adjustments). Used by the alarm scheduler.
     */
    suspend fun calculatePrayerDay(
        location: GeoLocation,
        date: LocalDate
    ): PrayerDay {
        val prayerSettings = settings.prayerSettings.first()
        return calculator.calculate(
            location = location,
            date = date,
            method = prayerSettings.method,
            asrMethod = prayerSettings.asrMethod,
            highLatitudeMethod = prayerSettings.highLatitude,
            fajrAngleOverride = prayerSettings.fajrAngle.toDouble(),
            ishaAngleOverride = prayerSettings.ishaAngle.toDouble(),
            adjustments = prayerSettings.adjustmentMap()
        )
    }

    /** Fetch fresh location from GPS and cache it. Returns true on success. */
    suspend fun refreshLocation(): Boolean {
        val location = locationProvider.getCurrentLocation() ?: return false
        val geocoded = locationProvider.reverseGeocode(location)
        settings.saveLocation(
            SettingsRepository.StoredLocation(
                latitude = geocoded.latitude,
                longitude = geocoded.longitude,
                country = geocoded.country,
                city = geocoded.city,
                region = geocoded.region,
                timeZoneId = geocoded.timeZoneId,
                source = "gps"
            )
        )
        return true
    }

    suspend fun saveManualLocation(
        latitude: Double,
        longitude: Double,
        country: String?,
        city: String?,
        region: String? = null,
        timeZoneId: String
    ) {
        settings.saveLocation(
            SettingsRepository.StoredLocation(
                latitude = latitude,
                longitude = longitude,
                country = country,
                city = city,
                region = region,
                timeZoneId = timeZoneId,
                source = "manual"
            )
        )
    }

    /** Geocode a city name to coordinates using the Android Geocoder. Returns null if not found. */
    suspend fun geocodeCity(city: String, country: String?): Pair<Double, Double>? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val geocoder = android.location.Geocoder(
                    locationProvider.applicationContext(),
                    java.util.Locale.getDefault()
                )
                val results = geocoder.getFromLocationName(
                    if (country.isNullOrBlank()) city else "$city, $country", 1
                )
                results?.firstOrNull()?.let {
                    it.latitude to it.longitude
                }
            }.getOrNull()
        }
    }
}
