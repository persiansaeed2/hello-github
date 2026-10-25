package com.example.motalee.model

import java.time.LocalDate

/**
 * A date in the Jalali (Solar Hijri / شمسی) calendar.
 *
 * The instance is a plain value object; all conversion logic lives in [JalaliCalendar]
 * so it can be unit tested on the JVM without Android.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    /** Days since 1970-01-01, handy as a stable storage / grouping key. */
    val epochDay: Long get() = JalaliCalendar.toEpochDay(this)

    fun plusDays(days: Long): JalaliDate =
        JalaliCalendar.fromEpochDay(epochDay + days)

    /** 0 = شنبه … 6 = جمعه */
    val dayOfWeek: Int get() = JalaliCalendar.dayOfWeek(this)

    override fun toString(): String =
        "%04d/%02d/%02d".format(year, month, day)
}

/**
 * Conversion between the Jalali and Gregorian calendars.
 *
 * Port of the well known `jalaali-js` algorithm (integer math only, no allocations
 * apart from the returned date), verified against reference dates in JalaliDateTest.
 */
object JalaliCalendar {

    /** JDN of 1970-01-01 (Thursday). */
    private const val EPOCH_JDN = 2440588

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /** Saturday-first week, matching how Jalali calendars are printed. */
    val WEEK_DAYS = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    val WEEK_DAY_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    fun isLeapYear(year: Int): Boolean = jalCal(year).leap == 0

    fun monthLength(year: Int, month: Int): Int = when {
        month <= 6 -> 31
        month <= 11 -> 30
        isLeapYear(year) -> 30
        else -> 29
    }

    fun today(): JalaliDate = fromEpochDay(LocalDate.now().toEpochDay())

    fun toEpochDay(date: JalaliDate): Long = toJdn(date.year, date.month, date.day) - EPOCH_JDN

    fun fromEpochDay(epochDay: Long): JalaliDate = fromJdn((epochDay + EPOCH_JDN).toInt())

    /** 0 = شنبه … 6 = جمعه */
    fun dayOfWeek(date: JalaliDate): Int =
        Math.floorMod(toJdn(date.year, date.month, date.day).toLong() + 2L, 7L).toInt()

    /** Weekday index (0 = شنبه) of the first day of the given month. */
    fun firstDayOfWeek(year: Int, month: Int): Int = dayOfWeek(JalaliDate(year, month, 1))

    fun monthName(month: Int): String = MONTH_NAMES[month - 1]

    fun weekDayName(index: Int): String = WEEK_DAY_NAMES[index]

    /** e.g. "شنبه ۳ آبان ۱۴۰۵" */
    fun formatLong(date: JalaliDate): String =
        "${weekDayName(date.dayOfWeek)} ${date.day.fa} ${monthName(date.month)} ${date.year.fa}"

    /** e.g. "۳ آبان ۱۴۰۵" */
    fun formatShort(date: JalaliDate): String =
        "${date.day.fa} ${monthName(date.month)} ${date.year.fa}"

    /** e.g. "آبان ۱۴۰۵" */
    fun formatMonth(year: Int, month: Int): String =
        "${monthName(month)} ${year.fa}"

    // ---------------------------------------------------------------- internals

    private fun div(a: Int, b: Int): Int = a / b

    private fun mod(a: Int, b: Int): Int = a - (a / b) * b

    private data class YearInfo(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(year: Int): YearInfo {
        val gy = year + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0

        for (i in 1 until BREAKS.size) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (year < jm) break
            leapJ += div(jump, 33) * 8 + div(jump % 33, 4)
            jp = jm
        }

        var n = year - jp
        leapJ += div(n, 33) * 8 + div(n % 33 + 3, 4)
        if (jump % 33 == 4 && jump - n == 4) leapJ++

        val leapG = div(gy, 4) - div(div(gy, 100) + 1, 4) * 3 - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4

        return YearInfo(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
        d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752
        return d
    }

    private fun d2g(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return Triple(gy, gm, gd)
    }

    private fun toJdn(jy: Int, jm: Int, jd: Int): Int {
        val info = jalCal(jy)
        return g2d(info.gy, 3, info.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    private fun fromJdn(jdn: Int): JalaliDate {
        val gy = d2g(jdn).first
        var jy = gy - 621
        val info = jalCal(jy)
        val jdn1f = g2d(gy, 3, info.march)
        var k = jdn - jdn1f

        if (k >= 0) {
            if (k <= 185) return JalaliDate(jy, 1 + div(k, 31), 1 + mod(k, 31))
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (info.leap == 1) k += 1
        }
        return JalaliDate(jy, 7 + div(k, 30), 1 + mod(k, 30))
    }
}
