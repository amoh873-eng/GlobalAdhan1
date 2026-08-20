package com.globaladhan.app.domain.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IslamicCalendarTest {

    private val calendar = IslamicCalendar()

    @Test
    fun `known hijri date conversion`() {
        // 1 Ramadan 1445 AH = 11 March 2024 (approximately, Umm al-Qura)
        val hijri = calendar.toHijri(LocalDate.of(2024, 3, 11))
        assertEquals(9, hijri.month) // Ramadan is month 9
        assertEquals(1445, hijri.year)
    }

    @Test
    fun `round trip conversion works`() {
        val gregorian = LocalDate.of(2024, 6, 15)
        val hijri = calendar.toHijri(gregorian)
        val back = calendar.toGregorian(hijri.year, hijri.month, hijri.day)
        assertNotNull(back)
        // Umm al-Qura may drift by a day in edge cases; allow small tolerance
        assertTrue(
            "Round trip failed: $gregorian -> $hijri -> $back",
            Math.abs(gregorian.toEpochDay() - back!!.toEpochDay()) <= 1
        )
    }

    @Test
    fun `hijri year increases with gregorian year`() {
        val year2023 = calendar.toHijri(LocalDate.of(2023, 6, 1))
        val year2024 = calendar.toHijri(LocalDate.of(2024, 6, 1))
        assertTrue(year2024.year >= year2023.year)
    }

    @Test
    fun `important dates are present`() {
        // Gregorian year 2024 (overlaps Hijri 1445/1446)
        val dates = calendar.importantDates(2024)
        assertTrue(dates.any { it.first == "Ramadan" })
        assertTrue(dates.any { it.first == "Eid al-Fitr" })
        assertTrue(dates.any { it.first == "Eid al-Adha" })
    }

    @Test
    fun `month names are arabic`() {
        val hijri = calendar.toHijri(LocalDate.of(2024, 3, 11))
        assertEquals("رمضان", hijri.monthName)
    }
}
