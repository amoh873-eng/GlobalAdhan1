package com.globaladhan.app.domain.prayer

import com.globaladhan.app.domain.model.AsrMethod
import com.globaladhan.app.domain.model.CalculationMethod
import com.globaladhan.app.domain.model.GeoLocation
import com.globaladhan.app.domain.model.HighLatitudeMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Reference accuracy tests for the prayer engine.
 *
 * Reference values are cross-checked against the Aladhan.com API (which uses
 * the well-tested PrayTimes engine) and published prayer timetables. Tolerances
 * reflect methodology differences (e.g. rounding, angle definitions), not
 * mathematical error.
 */
class PrayerTimeReferenceTest {

    private val calculator = PrayerTimeCalculator()

    private fun loc(lat: Double, lon: Double, tz: String) = GeoLocation(lat, lon, timeZoneId = tz)

    private fun minutes(t: LocalTime): Int = t.toSecondOfDay() / 60

    /** Assert a prayer time is within [toleranceMinutes] of the reference. */
    private fun assertClose(
        name: String,
        actual: LocalTime,
        reference: String,
        toleranceMinutes: Int = 2
    ) {
        val ref = LocalTime.parse(reference)
        val diff = kotlin.math.abs(minutes(actual) - minutes(ref))
        assertTrue(
            "$name: expected ~$reference, got $actual (diff ${diff}m)",
            diff <= toleranceMinutes
        )
    }

    // --- Reference dataset (date, city, coords, method) ---

    @Test
    fun `amman reference times - 2024-06-21 MWL`() {
        val day = calculator.calculate(
            loc(31.95, 35.93, "Asia/Amman"),
            LocalDate.of(2024, 6, 21),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        // Ground truth (Aladhan, MWL, 2024-06-21):
        // Fajr 03:51, Sunrise 05:31, Dhuhr 12:38, Asr 16:18, Maghrib 19:45, Isha 21:19
        assertClose("Fajr", day.fajr, "03:51", 3)
        assertClose("Sunrise", day.sunrise, "05:31", 2)
        assertClose("Dhuhr", day.dhuhr, "12:38", 2)
        assertClose("Asr", day.asr, "16:18", 3)
        assertClose("Maghrib", day.maghrib, "19:45", 2)
        assertClose("Isha", day.isha, "21:19", 3)
    }

    @Test
    fun `mecca reference times - 2024-01-01 Umm al-Qura`() {
        val day = calculator.calculate(
            loc(21.4225, 39.8262, "Asia/Riyadh"),
            LocalDate.of(2024, 1, 1),
            method = CalculationMethod.UMM_AL_QURA
        )
        // Ground truth (Aladhan API, Umm al-Qura, 2024-01-01):
        // Fajr 05:37, Sunrise 06:58, Dhuhr 12:24, Asr 15:28, Maghrib 17:50, Isha 19:20
        assertClose("Fajr", day.fajr, "05:37", 3)
        assertClose("Sunrise", day.sunrise, "06:58", 2)
        assertClose("Dhuhr", day.dhuhr, "12:24", 2)
        assertClose("Asr", day.asr, "15:28", 3)
        assertClose("Maghrib", day.maghrib, "17:50", 2)
        assertClose("Isha", day.isha, "19:20", 3)
    }

    @Test
    fun `istanbul reference times - 2024-03-20 MWL`() {
        val day = calculator.calculate(
            loc(41.0082, 28.9784, "Europe/Istanbul"),
            LocalDate.of(2024, 3, 20),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        // Ground truth (Aladhan, MWL, 2024-03-20):
        // Fajr 05:35, Sunrise 07:07, Dhuhr 13:11, Asr 16:36, Maghrib 19:17, Isha 20:43
        assertClose("Fajr", day.fajr, "05:35", 3)
        assertClose("Sunrise", day.sunrise, "07:07", 2)
        assertClose("Dhuhr", day.dhuhr, "13:11", 2)
        assertClose("Asr", day.asr, "16:36", 3)
        assertClose("Maghrib", day.maghrib, "19:17", 2)
        assertClose("Isha", day.isha, "20:43", 3)
    }

    @Test
    fun `karachi reference times - 2024-12-21 Karachi method`() {
        val day = calculator.calculate(
            loc(24.8607, 67.0011, "Asia/Karachi"),
            LocalDate.of(2024, 12, 21),
            method = CalculationMethod.KARACHI
        )
        // Ground truth (Aladhan, Karachi, 2024-12-21):
        // Fajr 05:51, Sunrise 07:12, Dhuhr 12:30, Asr 15:28, Maghrib 17:48, Isha 19:10
        assertClose("Fajr", day.fajr, "05:51", 3)
        assertClose("Sunrise", day.sunrise, "07:12", 2)
        assertClose("Dhuhr", day.dhuhr, "12:30", 2)
        assertClose("Asr", day.asr, "15:28", 3)
        assertClose("Maghrib", day.maghrib, "17:48", 2)
        assertClose("Isha", day.isha, "19:10", 3)
    }

    @Test
    fun `new york reference times - 2024-07-04 ISNA`() {
        val day = calculator.calculate(
            loc(40.7128, -74.0060, "America/New_York"),
            LocalDate.of(2024, 7, 4),
            method = CalculationMethod.NORTH_AMERICA
        )
        // Ground truth (Aladhan, ISNA, 2024-07-04):
        // Fajr 03:52, Sunrise 05:31, Dhuhr 13:01, Asr 17:00, Maghrib 20:30, Isha 22:09
        assertClose("Fajr", day.fajr, "03:52", 3)
        assertClose("Sunrise", day.sunrise, "05:31", 2)
        assertClose("Dhuhr", day.dhuhr, "13:01", 2)
        assertClose("Asr", day.asr, "17:00", 3)
        assertClose("Maghrib", day.maghrib, "20:30", 2)
        assertClose("Isha", day.isha, "22:09", 3)
    }

    @Test
    fun `custom fajr and isha angles are applied`() {
        val base = calculator.calculate(
            loc(24.86, 67.0, "Asia/Karachi"),
            LocalDate.of(2024, 6, 1),
            method = CalculationMethod.CUSTOM
        )
        val custom = calculator.calculate(
            loc(24.86, 67.0, "Asia/Karachi"),
            LocalDate.of(2024, 6, 1),
            method = CalculationMethod.CUSTOM,
            fajrAngleOverride = 20.0,  // steeper than default 18
            ishaAngleOverride = 19.0   // steeper than default 17
        )
        // Steeper angle → earlier Fajr, later Isha
        assertTrue("Custom Fajr should be earlier", custom.fajr < base.fajr)
        assertTrue("Custom Isha should be later", custom.isha > base.isha)
    }

    @Test
    fun `per-prayer adjustments apply to all six prayers`() {
        val base = calculator.calculate(
            loc(31.95, 35.93, "Asia/Amman"),
            LocalDate.of(2024, 6, 21)
        )
        val adjusted = calculator.calculate(
            loc(31.95, 35.93, "Asia/Amman"),
            LocalDate.of(2024, 6, 21),
            adjustments = mapOf(
                com.globaladhan.app.domain.model.PrayerName.FAJR to 2,
                com.globaladhan.app.domain.model.PrayerName.SUNRISE to -1,
                com.globaladhan.app.domain.model.PrayerName.DHUHR to 0,
                com.globaladhan.app.domain.model.PrayerName.ASR to -1,
                com.globaladhan.app.domain.model.PrayerName.MAGHRIB to 1,
                com.globaladhan.app.domain.model.PrayerName.ISHA to 2
            )
        )
        assertEquals(base.fajr.plusMinutes(2), adjusted.fajr)
        assertEquals(base.sunrise.minusMinutes(1), adjusted.sunrise)
        assertEquals(base.asr.minusMinutes(1), adjusted.asr)
        assertEquals(base.maghrib.plusMinutes(1), adjusted.maghrib)
        assertEquals(base.isha.plusMinutes(2), adjusted.isha)
    }

    // --- High latitude ---

    @Test
    fun `london winter - no adjustment needed - sane times`() {
        val day = calculator.calculate(
            loc(51.5074, -0.1278, "Europe/London"),
            LocalDate.of(2024, 1, 15),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        assertTrue(day.fajr < day.sunrise)
        assertTrue(day.sunrise < day.dhuhr)
        assertTrue(day.dhuhr < day.asr)
        assertTrue(day.asr < day.maghrib)
        assertTrue(day.maghrib < day.isha)
    }

    @Test
    fun `tromso summer - fajr and isha are distinct and reachable`() {
        val day = calculator.calculate(
            loc(69.6492, 18.9553, "Europe/Oslo"),
            LocalDate.of(2024, 6, 1),
            highLatitudeMethod = HighLatitudeMethod.MIDDLE_OF_THE_NIGHT
        )
        // Middle-of-night: Fajr and Isha must never be forced equal.
        assertTrue("Fajr=$day.fajr Isha=$day.isha must differ", day.fajr != day.isha)
        assertTrue(day.maghrib < day.isha)
        assertTrue(day.fajr < day.sunrise || day.fajr.isAfter(day.maghrib))
    }

    @Test
    fun `reykjavik summer - seventh of night is valid`() {
        val day = calculator.calculate(
            loc(64.1466, -21.9426, "Atlantic/Reykjavik"),
            LocalDate.of(2024, 6, 21),
            highLatitudeMethod = HighLatitudeMethod.SEVENTH_OF_THE_NIGHT
        )
        assertTrue("Fajr=$day.fajr Isha=$day.isha must differ", day.fajr != day.isha)
        assertTrue(day.maghrib < day.isha)
    }

    @Test
    fun `fairbanks summer - middle of night never equals fajr to isha`() {
        val day = calculator.calculate(
            loc(64.8378, -147.7164, "America/Anchorage"),
            LocalDate.of(2024, 6, 1),
            highLatitudeMethod = HighLatitudeMethod.MIDDLE_OF_THE_NIGHT
        )
        assertTrue(day.fajr != day.isha)
        assertTrue(day.isha > day.maghrib)
    }
}
