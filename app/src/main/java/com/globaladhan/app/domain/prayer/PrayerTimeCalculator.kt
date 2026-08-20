package com.globaladhan.app.domain.prayer

import com.globaladhan.app.domain.model.AsrMethod
import com.globaladhan.app.domain.model.CalculationMethod
import com.globaladhan.app.domain.model.GeoLocation
import com.globaladhan.app.domain.model.HighLatitudeMethod
import com.globaladhan.app.domain.model.PrayerDay
import com.globaladhan.app.domain.model.PrayerName
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Prayer time calculation engine based on standard astronomical formulas
 * (sun position via Julian day calculations).
 *
 * The engine computes the sun's declination and equation of time for a given
 * date and uses them with the observer's latitude/longitude to derive the
 * hour angles for each prayer.
 *
 * Reference algorithms follow the well-documented approach used by
 * PrayTimes.org and the Adhan library (BSD licensed), which are themselves
 * derived from the accurate solar position model.
 */
class PrayerTimeCalculator {

    /**
     * Calculate prayer times for a single day.
     *
     * @param location geographic location
     * @param date local date
     * @param method calculation method (angles)
     * @param asrMethod standard or Hanafi shadow ratio
     * @param highLatitudeMethod adjustment for extreme latitudes
     * @param fajrAngleOverride user-defined custom Fajr angle (overrides the method)
     * @param ishaAngleOverride user-defined custom Isha angle (overrides the method)
     * @param adjustments manual minute offsets per prayer (positive = later)
     */
    fun calculate(
        location: GeoLocation,
        date: LocalDate,
        method: CalculationMethod = CalculationMethod.default,
        asrMethod: AsrMethod = AsrMethod.default,
        highLatitudeMethod: HighLatitudeMethod = HighLatitudeMethod.default,
        fajrAngleOverride: Double? = null,
        ishaAngleOverride: Double? = null,
        adjustments: Map<PrayerName, Int> = emptyMap()
    ): PrayerDay {
        // Julian day for local noon of the target date
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth)
        val sunPosition = sunPosition(jd)
        val declination = sunPosition.first
        val equationOfTimeMinutes = sunPosition.second

        val lat = location.latitude
        val lng = location.longitude

        // Compute sun altitude angle at the given time fraction
        fun sunAngleAt(timeFraction: Double): Double {
            val t = timeFraction + lng / 15.0 // convert local solar to UTC
            val hourAngle = (t - 12.0) * 15.0
            val sinAlt = sin(degToRad(declination)) * sin(degToRad(lat)) +
                cos(degToRad(declination)) * cos(degToRad(lat)) * cos(degToRad(hourAngle))
            return radToDeg(asinSafe(sinAlt))
        }

        // Dhuhr: solar noon (12:00 − equation of time), corrected for longitude
        // offset from the time zone meridian. The equation of time is negative
        // when the sun runs fast (e.g. ~−3.3 min in early January), so subtracting
        // it correctly delays solar noon to ~12:03+ in that period.
        val zone = runCatching { ZoneId.of(location.timeZoneId) }
            .getOrDefault(ZoneId.systemDefault())
        val offsetHours = zoneOffsetHours(date, zone)
        val meridianLon = offsetHours * 15.0 // time zone central meridian
        val dhuhrHours = 12.0 - equationOfTimeMinutes / 60.0 + (meridianLon - lng) / 15.0
        val dhuhr = hourToLocalTime(dhuhrHours)

        // Effective angles: user-defined custom values take precedence over the method.
        val fajrAngle = fajrAngleOverride ?: method.fajrAngle ?: 18.0
        val ishaAngle = ishaAngleOverride ?: method.ishaAngle ?: 17.0

        // Fajr: when sun is at -fajrAngle
        val fajrUnreachable = !reachesAngle(fajrAngle, declination, lat)
        val fajrHour = hourAngle(fajrAngle, declination, lat, afterNoon = false)
        val fajr = dhuhr.plusSeconds((fajrHour * 3600.0).toLong())

        // Sunrise: sun at -0.833 degrees
        val sunriseHour = hourAngle(0.833, declination, lat, afterNoon = false)
        val sunrise = dhuhr.plusSeconds((sunriseHour * 3600.0).toLong())

        // Asr: shadow ratio based on method.
        // Asr begins when the shadow length equals the object's length (standard)
        // or twice the length (Hanafi). Sun altitude: atan(1 / (factor + tan|φ−δ|)).
        val asrFactor = when (asrMethod) {
            AsrMethod.STANDARD -> 1.0
            AsrMethod.HANAFI -> 2.0
        }
        val asrAltitude = radToDeg(
            atan(1.0 / (asrFactor + tan(degToRad(abs(lat - declination)))))
        )
        val asrZenith = 90.0 - asrAltitude // zenith distance in degrees
        val asrHour = hourAngleFromZenith(asrZenith, declination, lat, afterNoon = true)
        val asr = dhuhr.plusSeconds((asrHour * 3600.0).toLong())

        // Maghrib: sunset
        val maghribHour = hourAngle(0.833, declination, lat, afterNoon = true)
        val maghrib = dhuhr.plusSeconds((maghribHour * 3600.0).toLong())

        // Isha: angle or fixed minutes after maghrib
        val ishaUnreachable = method.ishaMinutes == null &&
            !reachesAngle(ishaAngle, declination, lat)
        val isha: LocalTime = when {
            method.ishaMinutes != null -> maghrib.plusMinutes(method.ishaMinutes.toLong())
            else -> {
                val ishaHour = hourAngle(ishaAngle, declination, lat, afterNoon = true)
                dhuhr.plusSeconds((ishaHour * 3600.0).toLong())
            }
        }

        // Midnight: middle between maghrib and fajr
        val midnight = middleTime(maghrib, fajr)

        // High latitude adjustment: only needed when the sun does not reach the
        // required angle (cosH out of [-1, 1]). Each prayer is corrected
        // independently using the real night interval (sunset → next sunrise),
        // never by forcing Fajr and Isha to the same instant.
        val adjusted = if (fajrUnreachable || ishaUnreachable) {
            adjustForHighLatitude(
                fajr = fajr,
                sunrise = sunrise,
                maghrib = maghrib,
                isha = isha,
                method = highLatitudeMethod,
                fajrUnreachable = fajrUnreachable,
                ishaUnreachable = ishaUnreachable
            )
        } else {
            AdjustedTimes(fajr, sunrise, maghrib, isha)
        }

        fun applyAdjustment(name: PrayerName, time: LocalTime): LocalTime =
            time.plusMinutes(adjustments[name]?.toLong() ?: 0L)

        return PrayerDay(
            date = date,
            location = location,
            fajr = applyAdjustment(PrayerName.FAJR, adjusted.fajr),
            sunrise = applyAdjustment(PrayerName.SUNRISE, sunrise),
            dhuhr = applyAdjustment(PrayerName.DHUHR, dhuhr),
            asr = applyAdjustment(PrayerName.ASR, asr),
            maghrib = applyAdjustment(PrayerName.MAGHRIB, adjusted.maghrib),
            isha = applyAdjustment(PrayerName.ISHA, adjusted.isha),
            midnight = applyAdjustment(PrayerName.MIDNIGHT, midnight),
            calculationMethod = method,
            asrMethod = asrMethod,
            adjustments = adjustments
        )
    }

    /** Result of high-latitude adjustment. */
    private data class AdjustedTimes(
        val fajr: LocalTime,
        val sunrise: LocalTime,
        val maghrib: LocalTime,
        val isha: LocalTime
    )

    /**
     * True when the sun reaches the given [angle] below the horizon on this day,
     * i.e. the astronomical event is geometrically possible (cosH within [-1, 1]).
     */
    private fun reachesAngle(angle: Double, declination: Double, lat: Double): Boolean {
        val zenith = 90.0 + angle
        val cosH = (cos(degToRad(zenith)) - sin(degToRad(declination)) * sin(degToRad(lat))) /
            (cos(degToRad(declination)) * cos(degToRad(lat)))
        return cosH in -1.0..1.0
    }

    /**
     * Compute the hour angle when the sun reaches the given [angle] degrees
     * BELOW the horizon (i.e. altitude = -angle). Uses the standard zenith
     * formula: cos(H) = (cos(90°+angle) − sin(δ)·sin(φ)) / (cos(δ)·cos(φ)).
     */
    private fun hourAngle(angle: Double, declination: Double, lat: Double, afterNoon: Boolean): Double {
        val zenith = 90.0 + angle
        val cosH = (cos(degToRad(zenith)) - sin(degToRad(declination)) * sin(degToRad(lat))) /
            (cos(degToRad(declination)) * cos(degToRad(lat)))
        val h = radToDeg(acosSafe(cosH)) / 15.0
        return if (afterNoon) h else -h
    }

    /**
     * Compute the hour angle for a given solar [zenith] (zenith distance in degrees).
     * cos(H) = (cos(zenith) − sin(δ)·sin(φ)) / (cos(δ)·cos(φ)).
     */
    private fun hourAngleFromZenith(
        zenith: Double,
        declination: Double,
        lat: Double,
        afterNoon: Boolean
    ): Double {
        val cosH = (cos(degToRad(zenith)) - sin(degToRad(declination)) * sin(degToRad(lat))) /
            (cos(degToRad(declination)) * cos(degToRad(lat)))
        val h = radToDeg(acosSafe(cosH)) / 15.0
        return if (afterNoon) h else -h
    }

    private fun acosSafe(x: Double): Double {
        val clamped = x.coerceIn(-1.0, 1.0)
        return kotlin.math.acos(clamped)
    }

    private fun asinSafe(x: Double): Double {
        val clamped = x.coerceIn(-1.0, 1.0)
        return kotlin.math.asin(clamped)
    }

    private fun middleTime(first: LocalTime, second: LocalTime): LocalTime {
        val firstSec = first.toSecondOfDay().toLong()
        var secondSec = second.toSecondOfDay().toLong()
        if (secondSec < firstSec) secondSec += 86400L
        return LocalTime.ofSecondOfDay(((firstSec + secondSec) / 2) % 86400L)
    }

    private fun adjustForHighLatitude(
        fajr: LocalTime,
        sunrise: LocalTime,
        maghrib: LocalTime,
        isha: LocalTime,
        method: HighLatitudeMethod,
        fajrUnreachable: Boolean,
        ishaUnreachable: Boolean
    ): AdjustedTimes {
        if (method == HighLatitudeMethod.NONE) {
            return AdjustedTimes(fajr, sunrise, maghrib, isha)
        }

        // The real night interval: sunset (Maghrib) → next sunrise.
        var nightSeconds = sunrise.toSecondOfDay().toLong() - maghrib.toSecondOfDay().toLong()
        if (nightSeconds <= 0) nightSeconds += 86400L

        val adjustedFajr = if (fajrUnreachable) {
            when (method) {
                HighLatitudeMethod.MIDDLE_OF_THE_NIGHT ->
                    // Fajr = middle of the night (distinct from Isha unless the
                    // night is symmetric — never force equality).
                    maghrib.plusSeconds((nightSeconds * 0.5).toLong())
                HighLatitudeMethod.SEVENTH_OF_THE_NIGHT,
                HighLatitudeMethod.ONE_SEVENTH ->
                    maghrib.plusSeconds((nightSeconds * (1.0 - 1.0 / 7.0)).toLong())
                HighLatitudeMethod.ANGLE_BASED, HighLatitudeMethod.NONE -> fajr
            }
        } else fajr

        val adjustedIsha = if (ishaUnreachable) {
            when (method) {
                HighLatitudeMethod.MIDDLE_OF_THE_NIGHT ->
                    // Isha = one seventh of the night from sunset — a distinct,
                    // defensible value, never forced equal to Fajr.
                    maghrib.plusSeconds((nightSeconds * (1.0 / 7.0)).toLong())
                HighLatitudeMethod.SEVENTH_OF_THE_NIGHT,
                HighLatitudeMethod.ONE_SEVENTH ->
                    maghrib.plusSeconds((nightSeconds * (1.0 / 7.0)).toLong())
                HighLatitudeMethod.ANGLE_BASED, HighLatitudeMethod.NONE -> isha
            }
        } else isha

        return AdjustedTimes(adjustedFajr, sunrise, maghrib, adjustedIsha)
    }

    private fun hourToLocalTime(hours: Double): LocalTime {
        val seconds = (hours * 3600.0).toLong() % 86400L
        val safe = if (seconds < 0) seconds + 86400L else seconds
        return LocalTime.ofSecondOfDay(safe)
    }

    private fun zoneOffsetHours(date: LocalDate, zone: ZoneId): Double {
        val zdt = date.atStartOfDay(zone)
        return zdt.offset.totalSeconds / 3600.0
    }

    private fun degToRad(d: Double) = d * kotlin.math.PI / 180.0
    private fun radToDeg(r: Double) = r * 180.0 / kotlin.math.PI

    /**
     * Solar position: returns [declinationDegrees, equationOfTimeMinutes].
     * Based on the standard low-precision solar position algorithm.
     */
    fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = normalizeDeg(357.529 + 0.98560028 * d)
        val q = normalizeDeg(280.459 + 0.98564736 * d)
        val l = normalizeDeg(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        // Right ascension must be normalized to [0, 360) because atan2 returns [-180, 180]
        val raDeg = normalizeDeg(radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))))
        val ra = raDeg / 15.0
        val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
        // Equation of time in minutes (can be negative, no normalization)
        val eqTimeMinutes = (q / 15.0 - ra) * 60.0
        return Pair(decl, eqTimeMinutes)
    }

    private fun normalizeDeg(d: Double): Double = ((d % 360.0) + 360.0) % 360.0
    private fun normalizeHours(h: Double): Double = ((h % 24.0) + 24.0) % 24.0

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = (y / 100).toInt()
        val b = 2 - a + (a / 4).toInt()
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }
}
