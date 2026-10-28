package com.example.solmaz.data

import android.content.Context

/**
 * The learner's own data that is not vocabulary progress: currently the name used by
 * the greeting on the today screen.
 */
class ProfileStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var name: String?
        get() = prefs.getString(KEY_NAME, null)?.takeIf { it.isNotBlank() }
        set(value) {
            val clean = value?.trim()?.take(30).orEmpty()
            prefs.edit().apply {
                if (clean.isEmpty()) remove(KEY_NAME) else putString(KEY_NAME, clean)
            }.apply()
        }

    /** Speech speed of the pronunciation; calmer than the Android default (1.0). */
    var speechRate: Float
        get() = prefs.getFloat(KEY_RATE, 0.7f).coerceIn(0.3f, 1.2f)
        set(value) {
            prefs.edit().putFloat(KEY_RATE, value).apply()
        }

    /** How many past days the progress chart shows; chosen by the learner. */
    var chartDays: Int
        get() = prefs.getInt(KEY_DAYS, 14).coerceIn(2, 60)
        set(value) {
            prefs.edit().putInt(KEY_DAYS, value.coerceIn(2, 60)).apply()
        }

    private companion object {
        const val KEY_DAYS = "chart_days"
        const val KEY_RATE = "speech_rate"
        const val PREFS_NAME = "solmaz_profile"
        const val KEY_NAME = "name"
    }
}
