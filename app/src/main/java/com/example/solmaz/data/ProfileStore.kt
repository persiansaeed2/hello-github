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

    private companion object {
        const val PREFS_NAME = "solmaz_profile"
        const val KEY_NAME = "name"
    }
}
