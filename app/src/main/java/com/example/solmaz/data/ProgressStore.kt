package com.example.solmaz.data

import android.content.Context
import com.example.solmaz.study.WordProgress
import org.json.JSONArray
import org.json.JSONObject

/** Where the Leitner state of every word lives. */
interface ProgressStore {
    fun load(): Map<String, WordProgress>
    fun save(progress: Map<String, WordProgress>)
}

/**
 * Saves `wordId -> box / next review day` as one small JSON document.
 * A vocabulary of a few thousand words is well below the size where a database
 * would be worth the extra dependency, and it keeps the whole app offline.
 */
class PrefsProgressStore(context: Context) : ProgressStore {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): Map<String, WordProgress> {
        val raw = prefs.getString(KEY, null) ?: return emptyMap()
        return runCatching {
            val array = JSONArray(raw)
            val result = LinkedHashMap<String, WordProgress>(array.length())
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val id = item.optString(KEY_ID).takeIf { it.isNotEmpty() } ?: continue
                result[id] = WordProgress(
                    wordId = id,
                    box = item.optInt(KEY_BOX, 1),
                    dueEpochDay = if (item.has(KEY_DUE) && !item.isNull(KEY_DUE)) item.optLong(KEY_DUE) else null,
                    lastReviewedEpochDay = if (item.has(KEY_LAST) && !item.isNull(KEY_LAST)) item.optLong(KEY_LAST) else null,
                    correctCount = item.optInt(KEY_CORRECT, 0),
                    wrongCount = item.optInt(KEY_WRONG, 0)
                )
            }
            result
        }.getOrDefault(emptyMap())
    }

    override fun save(progress: Map<String, WordProgress>) {
        val array = JSONArray()
        progress.values.forEach { state ->
            array.put(
                JSONObject()
                    .put(KEY_ID, state.wordId)
                    .put(KEY_BOX, state.box)
                    .put(KEY_DUE, state.dueEpochDay ?: JSONObject.NULL)
                    .put(KEY_LAST, state.lastReviewedEpochDay ?: JSONObject.NULL)
                    .put(KEY_CORRECT, state.correctCount)
                    .put(KEY_WRONG, state.wrongCount)
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "solmaz_progress"
        const val KEY = "progress"
        const val KEY_ID = "id"
        const val KEY_BOX = "box"
        const val KEY_DUE = "due"
        const val KEY_LAST = "last"
        const val KEY_CORRECT = "correct"
        const val KEY_WRONG = "wrong"
    }
}
