package com.example.motalee.data

import android.content.Context
import com.example.motalee.model.StudySession
import org.json.JSONArray
import org.json.JSONObject

/**
 * Tiny persistence layer on top of SharedPreferences.
 *
 * The data set is a personal list of study sessions — small enough that a
 * single JSON document is simpler and faster than a database, and it keeps the
 * APK free of extra dependencies.
 */
class StudyRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<StudySession> {
        val raw = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return runCatching { parse(raw) }.getOrDefault(emptyList())
    }

    fun save(sessions: List<StudySession>) {
        val array = JSONArray()
        sessions.forEach { session ->
            array.put(
                JSONObject()
                    .put("id", session.id)
                    .put("day", session.epochDay)
                    .put("subject", session.subject)
                    .put("minutes", session.minutes)
            )
        }
        prefs.edit().putString(KEY_SESSIONS, array.toString()).apply()
    }

    private fun parse(raw: String): List<StudySession> {
        val array = JSONArray(raw)
        val result = ArrayList<StudySession>(array.length())
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val minutes = item.optInt("minutes", 0)
            if (minutes <= 0) continue
            result += StudySession(
                id = item.optLong("id", i.toLong()),
                epochDay = item.optLong("day", 0L),
                subject = item.optString("subject").ifBlank { DEFAULT_SUBJECT },
                minutes = minutes
            )
        }
        return result
    }

    private companion object {
        const val PREFS_NAME = "motalee_prefs"
        const val KEY_SESSIONS = "sessions"
        const val DEFAULT_SUBJECT = "مطالعه"
    }
}
