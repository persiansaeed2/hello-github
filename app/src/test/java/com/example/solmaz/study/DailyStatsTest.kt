package com.example.solmaz.study

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyStatsTest {
    @Test fun daysBeforeStartAreIgnored() {
        assertEquals(10, DailyStats.average(listOf(0, 0, 5, 15), ignoredLeading = 2))
    }

    @Test fun zeroDaysAfterStartCount() {
        assertEquals(5, DailyStats.average(listOf(5, 0, 10), ignoredLeading = 0))
    }

    @Test fun emptyOrNoActiveDaysIsZero() {
        assertEquals(0, DailyStats.average(listOf(0, 0), ignoredLeading = 5))
    }
}
