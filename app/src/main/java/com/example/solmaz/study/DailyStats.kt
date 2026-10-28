package com.example.solmaz.study

object DailyStats {
    /**
     * Average words per day. [counts] runs oldest to newest; the first [ignoredLeading] days
     * are before the user started, so they are not counted. Days with zero words after the
     * start do count, so the number stays honest.
     */
    fun average(counts: List<Int>, ignoredLeading: Int): Int {
        val active = counts.drop(ignoredLeading.coerceIn(0, counts.size))
        if (active.isEmpty()) return 0
        return Math.round(active.sum().toDouble() / active.size).toInt()
    }
}
