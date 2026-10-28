package com.example.solmaz.data

import android.content.Context

/** Which words were read on which day, so the progress chart can count words per day. */
class DailyLog(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("solmaz_daily", Context.MODE_PRIVATE)

    /** A word counts once per day, however many times it is answered. */
    fun add(epochDay: Long, wordId: String) {
        val key = "d$epochDay"
        val ids = prefs.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        val first = firstDay()
        prefs.edit().apply {
            if (ids.add(wordId)) putStringSet(key, ids)
            if (first == null || epochDay < first) putLong("first", epochDay)
        }.apply()
    }

    fun count(epochDay: Long): Int = prefs.getStringSet("d$epochDay", null)?.size ?: 0

    fun firstDay(): Long? = if (prefs.contains("first")) prefs.getLong("first", 0L) else null
}
