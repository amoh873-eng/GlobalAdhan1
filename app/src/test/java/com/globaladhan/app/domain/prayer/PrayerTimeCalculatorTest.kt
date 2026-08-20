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

class PrayerTimeCalculatorTest {

    private val calculator = PrayerTimeCalculator()

    // Makkah, Saudi Arabia
    private val makkah = GeoLocation(
        latitude = 21.422487,
        longitude = 39.826206,
        timeZoneId = "Asia/Riyadh"
    )

    // London, UK
    private val london = GeoLocation(
        latitude = 51.5074,
        longitude = -0.1278,
        timeZoneId = "Europe/London"
    )

    // Jakarta, Indonesia
    private val jakarta = GeoLocation(
        latitude = -6.2088,
        longitude = 106.8456,
        timeZoneId = "Asia/Jakarta"
    )

    @Test
    fun `prayer times are valid for Makkah`() {
        val day = calculator.calculate(
            location = makkah,
            date = LocalDate.of(2024, 3, 20),
            method = CalculationMethod.UMM_AL_QURA
        )
        // Sanity checks
        assertTrue(day.fajr < day.sunrise)
        assertTrue(day.sunrise < day.dhuhr)
        assertTrue(day.dhuhr < day.asr)
        assertTrue(day.asr < day.maghrib)
        assertTrue(day.maghrib < day.isha)
    }

    @Test
    fun `dhuhr is around solar noon`() {
        val day = calculator.calculate(
            location = makkah,
            date = LocalDate.of(2024, 6, 15),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        // Dhuhr should be within 12:00 ± 1 hour for Makkah
        val dhuhrMinutes = day.dhuhr.toSecondOfDay() / 60
        assertTrue("Dhuhr=$day.dhuhr", dhuhrMinutes in 660..780)
    }

    @Test
    fun `hanafi asr is later than standard asr`() {
        val standard = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 7, 1),
            asrMethod = AsrMethod.STANDARD
        )
        val hanafi = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 7, 1),
            asrMethod = AsrMethod.HANAFI
        )
        assertTrue(hanafi.asr > standard.asr)
    }

    @Test
    fun `different methods produce different fajr`() {
        val mwl = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 1, 15),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        val northAmerica = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 1, 15),
            method = CalculationMethod.NORTH_AMERICA
        )
        // Different Fajr angles (18 vs 15) should yield different times
        assertTrue(mwl.fajr != northAmerica.fajr)
    }

    @Test
    fun `jakarta times are valid`() {
        val day = calculator.calculate(
            location = jakarta,
            date = LocalDate.of(2024, 8, 1),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        assertTrue(day.fajr < day.dhuhr)
        assertTrue(day.asr < day.maghrib)
    }

    @Test
    fun `makkah times match reference within tolerance`() {
        // Reference values for Makkah on 2024-03-20 (Umm al-Qura) from Aladhan.com
        // These are the widely published times for that date.
        val day = calculator.calculate(
            location = makkah,
            date = LocalDate.of(2024, 3, 20),
            method = CalculationMethod.UMM_AL_QURA
        )
        // Fajr ~05:05, Dhuhr ~12:23, Asr ~15:43, Maghrib ~18:25, Isha ~19:55 (approx)
        // Allow generous ±30 min tolerance since this is a sanity reference
        val fajrMin = day.fajr.toSecondOfDay() / 60
        val dhuhrMin = day.dhuhr.toSecondOfDay() / 60
        val asrMin = day.asr.toSecondOfDay() / 60
        val maghribMin = day.maghrib.toSecondOfDay() / 60
        val ishaMin = day.isha.toSecondOfDay() / 60

        assertTrue("Fajr=$fajrMin", fajrMin in 285..345)     // 04:45-05:45
        assertTrue("Dhuhr=$dhuhrMin", dhuhrMin in 730..770)  // 12:10-12:50
        assertTrue("Asr=$asrMin", asrMin in 910..960)        // 15:10-16:00
        assertTrue("Maghrib=$maghribMin", maghribMin in 1090..1130) // 18:10-18:50
        assertTrue("Isha=$ishaMin", ishaMin in 1170..1210)   // 19:30-20:10
    }

    @Test
    fun `manual adjustments shift times`() {
        val base = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 3, 1)
        )
        val adjusted = calculator.calculate(
            location = london,
            date = LocalDate.of(2024, 3, 1),
            adjustments = mapOf(
                com.globaladhan.app.domain.model.PrayerName.FAJR to 5,
                com.globaladhan.app.domain.model.PrayerName.MAGHRIB to -3
            )
        )
        assertEquals(base.fajr.plusMinutes(5), adjusted.fajr)
        assertEquals(base.maghrib.minusMinutes(3), adjusted.maghrib)
    }

    @Test
    fun `northern latitude does not crash`() {
        val tromso = GeoLocation(69.6492, 18.9553, timeZoneId = "Europe/Oslo")
        val day = calculator.calculate(
            location = tromso,
            date = LocalDate.of(2024, 6, 1),
            highLatitudeMethod = HighLatitudeMethod.MIDDLE_OF_THE_NIGHT
        )
        // Spec: Fajr and Isha must never be forced equal by the high-latitude method.
        assertTrue("Fajr=$day.fajr Isha=$day.isha must differ", day.fajr != day.isha)
        // Maghrib must be before Isha and Fajr must be before sunrise.
        assertTrue(day.maghrib < day.isha)
        assertTrue(day.fajr < day.sunrise || day.fajr.isAfter(day.maghrib))
    }

    @Test
    fun `summer vs winter daylight duration differs`() {
        val summer = calculator.calculate(location = london, date = LocalDate.of(2024, 6, 21))
        val winter = calculator.calculate(location = london, date = LocalDate.of(2024, 12, 21))
        val summerDaylight = summer.maghrib.toSecondOfDay() - summer.sunrise.toSecondOfDay()
        val winterDaylight = winter.maghrib.toSecondOfDay() - winter.sunrise.toSecondOfDay()
        assertTrue("Summer should have longer days", summerDaylight > winterDaylight)
    }
}
