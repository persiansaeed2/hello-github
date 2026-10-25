package com.example.solmaz.study

import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads the vocabulary file the book was exported to.
 *
 * The export shape is not fixed yet, so the parser is deliberately tolerant. All of
 * these are accepted:
 *
 * ```json
 * [ {"word":"apple","meaning":"سیب","lesson":1} ]
 * { "words":   [ ... ] }
 * { "lessons": [ {"number":1,"words":[...]}, {"number":2,"words":[...]} ] }
 * { "lessons": { "1": [ ... ], "2": [ ... ] } }
 * ```
 *
 * Key aliases (case-insensitive) are accepted too, e.g. `term/word/english/en`,
 * `meaning/translation/persian/fa`, `lesson/dars/unit/chapter` and the Persian
 * equivalents — so a differently shaped export still imports.
 *
 * Words without a term or meaning are skipped instead of failing the whole file;
 * the parser only fails when nothing usable is left.
 */
object WordJsonParser {

    private val TERM_KEYS = listOf("word", "term", "english", "en", "text", "لغت", "کلمه", "واژه")
    private val MEANING_KEYS =
        listOf("meaning", "translation", "persian", "fa", "farsi", "definition", "معنی", "ترجمه")
    private val LESSON_KEYS = listOf("lesson", "dars", "unit", "chapter", "book_lesson", "درس")
    private val ID_KEYS = listOf("id", "key", "code")
    private val LESSON_NUMBER_KEYS = listOf("number", "lesson", "dars", "id", "no", "شماره", "درس")

    sealed interface Result {
        data class Success(val words: List<Word>, val skipped: Int) : Result
        data class Failure(val message: String) : Result
    }

    fun parse(raw: String): Result {
        val text = raw.trim().removePrefix("\uFEFF").trim()
        if (text.isEmpty()) return Result.Failure("فایل خالی است")

        return try {
            when {
                text.startsWith("[") -> fromArray(JSONArray(text), lessonHint = null)
                text.startsWith("{") -> fromObject(JSONObject(text))
                else -> Result.Failure("فایل JSON معتبر نیست (با [ یا { شروع نمی‌شود)")
            }
        } catch (error: Exception) {
            Result.Failure("JSON خراب است: ${error.message}")
        }
    }

    private fun fromObject(root: JSONObject): Result {
        // 1) { "words": [...] }
        firstArrayOf(root, listOf("words", "loghat", "data", "items", "لغات"))
            ?.let { return fromArray(it, lessonHint = null) }

        // 2) { "lessons": [...] } or { "lessons": { "1": [...] } }
        firstValueOf(root, listOf("lessons", "dars", "units", "chapters", "درس‌ها"))?.let { lessons ->
            return when (lessons) {
                is JSONArray -> fromLessonsArray(lessons)
                is JSONObject -> fromLessonsMap(lessons)
                else -> Result.Failure("کلید lessons باید آرایه یا آبجکت باشد")
            }
        }

        // 3) a single word object
        parseWord(root, lessonHint = null, order = 0)?.let { word ->
            return Result.Success(listOf(word), skipped = 0)
        }

        return Result.Failure("ساختار فایل شناخته نشد")
    }

    private fun fromArray(array: JSONArray, lessonHint: Int?): Result {
        val words = ArrayList<Word>(array.length())
        var skipped = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index)
            val word = item?.let { parseWord(it, lessonHint, order = index) }
            if (word == null) skipped++ else words += word
        }
        return finish(words, skipped)
    }

    private fun fromLessonsArray(lessons: JSONArray): Result {
        val words = ArrayList<Word>()
        var skipped = 0
        for (index in 0 until lessons.length()) {
            val lessonObject = lessons.optJSONObject(index)
            if (lessonObject == null) {
                skipped++
                continue
            }
            val lessonNumber = lessonNumber(lessonObject, fallback = index + 1)
            val inner = firstArrayOf(lessonObject, listOf("words", "loghat", "items", "لغات"))
            if (inner == null) {
                skipped++
                continue
            }
            for (i in 0 until inner.length()) {
                val word = inner.optJSONObject(i)?.let { parseWord(it, lessonNumber, order = words.size) }
                if (word == null) skipped++ else words += word
            }
        }
        return finish(words, skipped)
    }

    private fun fromLessonsMap(map: JSONObject): Result {
        val words = ArrayList<Word>()
        var skipped = 0
        for (key in map.keys().asSequence().toList()) {
            val lessonNumber = key.toIntOrNull() ?: 1
            val inner = map.optJSONArray(key)
            if (inner == null) {
                skipped++
                continue
            }
            for (i in 0 until inner.length()) {
                val word = inner.optJSONObject(i)?.let { parseWord(it, lessonNumber, order = words.size) }
                if (word == null) skipped++ else words += word
            }
        }
        return finish(words, skipped)
    }

    private fun finish(words: List<Word>, skipped: Int): Result {
        if (words.isEmpty()) return Result.Failure("هیچ لغت سالمی پیدا نشد")
        val sorted = words.sortedWith(compareBy({ it.lesson }, { it.order }))
        return Result.Success(sorted, skipped)
    }

    private fun parseWord(json: JSONObject, lessonHint: Int?, order: Int): Word? {
        val term = valueOf(json, TERM_KEYS)?.trim().orEmpty()
        val meaning = valueOf(json, MEANING_KEYS)?.trim().orEmpty()
        if (term.isEmpty() || meaning.isEmpty()) return null

        val lesson = lessonHint
            ?: valueOf(json, LESSON_KEYS)?.trim()?.toIntOrNull()
            ?: 1
        val id = valueOf(json, ID_KEYS)?.trim()?.takeIf { it.isNotEmpty() } ?: "L$lesson:$term"

        return Word(id = id, term = term, meaning = meaning, lesson = lesson, order = order)
    }

    private fun lessonNumber(lessonObject: JSONObject, fallback: Int): Int =
        valueOf(lessonObject, LESSON_NUMBER_KEYS)?.trim()?.toIntOrNull() ?: fallback

    /** Case-insensitive lookup of the first matching key. */
    private fun valueOf(json: JSONObject, keys: List<String>): String? {
        val actualKeys = json.keys().asSequence().toList()
        for (candidate in keys) {
            val match = actualKeys.firstOrNull { it.equals(candidate, ignoreCase = true) } ?: continue
            val value = json.opt(match)
            if (value == null || value == JSONObject.NULL) continue
            return value.toString()
        }
        return null
    }

    private fun firstValueOf(json: JSONObject, keys: List<String>): Any? {
        val actualKeys = json.keys().asSequence().toList()
        for (candidate in keys) {
            val match = actualKeys.firstOrNull { it.equals(candidate, ignoreCase = true) } ?: continue
            val value = json.opt(match)
            if (value != null && value != JSONObject.NULL) return value
        }
        return null
    }

    private fun firstArrayOf(json: JSONObject, keys: List<String>): JSONArray? =
        firstValueOf(json, keys) as? JSONArray
}
