package com.example.motalee.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Reference values come from the official Iranian calendar (and are cross-checked
 * against java.time for the Gregorian side).
 */
class JalaliDateTest {

    private fun assertConversion(gy: Int, gm: Int, gd: Int, jy: Int, jm: Int, jd: Int) {
        val gregorianEpochDay = LocalDate.of(gy, gm, gd).toEpochDay()
        assertEquals(
            "Gregorian -> Jalali for $gy-$gm-$gd",
            JalaliDate(jy, jm, jd),
            JalaliCalendar.fromEpochDay(gregorianEpochDay)
        )
        assertEquals(
            "Jalali -> Gregorian for $jy-$jm-$jd",
            gregorianEpochDay,
            JalaliCalendar.toEpochDay(JalaliDate(jy, jm, jd))
        )
    }

    @Test
    fun `known dates convert both ways`() {
        // Nowruz of several years
        assertConversion(2021, 3, 21, 1400, 1, 1)
        assertConversion(2024, 3, 20, 1403, 1, 1)
        assertConversion(2026, 3, 21, 1405, 1, 1)
        // Start of the second half of the year (Mehr 1st)
        assertConversion(2023, 9, 23, 1402, 7, 1)
        // Historic / everyday dates
        assertConversion(1979, 2, 11, 1357, 11, 22)
        assertConversion(2000, 1, 1, 1378, 10, 11)
        // Today's session date (1405 Aban 3)
        assertConversion(2026, 10, 25, 1405, 8, 3)
    }

    @Test
    fun `every day of a 20 year window round trips`() {
        val start = LocalDate.of(2015, 1, 1).toEpochDay()
        for (offset in 0 until 7305) {
            val epochDay = start + offset
            val jalali = JalaliCalendar.fromEpochDay(epochDay)
            assertEquals(epochDay, JalaliCalendar.toEpochDay(jalali))
        }
    }

    @Test
    fun `month lengths follow the jalali rules`() {
        assertTrue((1..6).all { JalaliCalendar.monthLength(1405, it) == 31 })
        assertTrue((7..11).all { JalaliCalendar.monthLength(1405, it) == 30 })
        // 1403 is a leap year, 1404 is not.
        assertEquals(30, JalaliCalendar.monthLength(1403, 12))
        assertEquals(29, JalaliCalendar.monthLength(1404, 12))
        assertTrue(JalaliCalendar.isLeapYear(1403))
        assertFalse(JalaliCalendar.isLeapYear(1404))
    }

    @Test
    fun `week starts on saturday`() {
        // 1405/08/01 = 2026-10-23 was a Friday -> index 6 of a Saturday-first week.
        assertEquals(6, JalaliCalendar.firstDayOfWeek(1405, 8))
        // 1405/08/03 = 2026-10-25 was a Sunday.
        assertEquals(1, JalaliDate(1405, 8, 3).dayOfWeek)
        assertEquals("یکشنبه", JalaliCalendar.weekDayName(1))
    }

    @Test
    fun `plusDays crosses month and year boundaries`() {
        val lastDayOfYear = JalaliDate(1404, 12, 29)
        assertEquals(JalaliDate(1405, 1, 1), lastDayOfYear.plusDays(1))
        assertEquals(JalaliDate(1404, 11, 30), JalaliDate(1404, 12, 1).plusDays(-1))
        assertEquals(JalaliDate(1404, 12, 29), JalaliDate(1403, 12, 30).plusDays(365))
    }

    @Test
    fun `formatting uses persian digits`() {
        val date = JalaliDate(1405, 8, 3)
        assertEquals("یکشنبه ۳ آبان ۱۴۰۵", JalaliCalendar.formatLong(date))
        assertEquals("۳ آبان ۱۴۰۵", JalaliCalendar.formatShort(date))
        assertEquals("آبان ۱۴۰۵", JalaliCalendar.formatMonth(1405, 8))
    }

    @Test
    fun `minute formatting reads naturally`() {
        assertEquals("۰ دقیقه", formatMinutes(0))
        assertEquals("۴۵ دقیقه", formatMinutes(45))
        assertEquals("۲ ساعت", formatMinutes(120))
        assertEquals("۱ ساعت و ۱۵ دقیقه", formatMinutes(75))
    }
}
