package com.globaladhan.app.domain.calendar

import com.globaladhan.app.domain.model.IslamicDate
import com.globaladhan.app.domain.model.IslamicMonth
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * Hijri (Islamic) calendar conversion using java.time.HijrahChronology.
 * The Umm al-Qura variant is used for Saudi Arabia; other regional variants
 * can be plugged in later without changing the UI.
 */
class IslamicCalendar {

    private val hijrahFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    /** Convert a Gregorian date to the Islamic (Hijri) date. */
    fun toHijri(gregorian: LocalDate): IslamicDate {
        val hijrahDate = HijrahDate.from(gregorian)
        val monthValue = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
        val month = IslamicMonth.byIndex(monthValue - 1)
        return IslamicDate(
            year = hijrahDate.get(ChronoField.YEAR),
            month = monthValue,
            day = hijrahDate.get(ChronoField.DAY_OF_MONTH),
            monthName = month.arabicName
        )
    }

    /** Convert a Hijri date to Gregorian. Returns null on invalid input. */
    fun toGregorian(year: Int, month: Int, day: Int): LocalDate? {
        return runCatching {
            val hijrahDate = HijrahChronology.INSTANCE.date(year, month, day)
            LocalDate.from(hijrahDate)
        }.getOrNull()
    }

    /** Human-readable localized Hijri date string. */
    fun formatHijri(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val hijrahDate = HijrahDate.from(date)
        return hijrahDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
        )
    }

    /**
     * Named important Islamic dates for a given Gregorian year (approximate).
     * Uses the Umm al-Qura calendar via HijrahChronology; results may differ
     * by a day in some regions that rely on moon sighting.
     */
    fun importantDates(gregorianYear: Int): List<Pair<String, LocalDate>> {
        // Derive the Hijri year that overlaps the middle of the given Gregorian year
        val midYearDate = LocalDate.of(gregorianYear, 6, 15)
        val hijriYear = toHijri(midYearDate).year
        return listOf(
            "Ramadan" to toGregorian(hijriYear, 9, 1),
            "Eid al-Fitr" to toGregorian(hijriYear, 10, 1),
            "Eid al-Adha" to toGregorian(hijriYear, 12, 10),
            "Day of Arafah" to toGregorian(hijriYear, 12, 9),
            "Ashura" to toGregorian(hijriYear, 1, 10),
            "Islamic New Year" to toGregorian(hijriYear, 1, 1),
            "Mawlid" to toGregorian(hijriYear, 3, 12)
        ).filter { it.second != null }.map { it.first to it.second!! }
    }
}
