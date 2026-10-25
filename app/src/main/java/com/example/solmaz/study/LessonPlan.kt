package com.example.solmaz.study

/** Summary of one lesson, ready to be rendered as a row in the lesson list. */
data class LessonOverview(
    val lesson: Int,
    val totalWords: Int,
    val newWords: Int,
    val dueWords: Int,
    val learnedWords: Int,
    val unlocked: Boolean
)

/**
 * Lessons are studied one after another, the way the book introduces them.
 *
 * Lesson 1 is always open; lesson N opens only after **every** word of lesson N-1 has
 * been read at least once (answered 🟢 or 🔴) — so no word stays unread and the last
 * lesson can never be pushed in front of the user during the first week.
 */
object LessonPlan {

    fun lessonNumbers(words: List<Word>): List<Int> =
        words.map { it.lesson }.distinct().sorted()

    fun isUnlocked(
        lesson: Int,
        words: List<Word>,
        progress: Map<String, WordProgress>
    ): Boolean {
        if (lesson <= 1) return true
        val previous = words.filter { it.lesson == lesson - 1 }
        if (previous.isEmpty()) return true
        return previous.all { word -> progress[word.id]?.isNew == false }
    }

    fun overview(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        todayEpochDay: Long
    ): List<LessonOverview> = lessonNumbers(words).map { lesson ->
        val lessonWords = words.filter { it.lesson == lesson }
        val states = lessonWords.map { progress[it.id] }
        LessonOverview(
            lesson = lesson,
            totalWords = lessonWords.size,
            newWords = lessonWords.count { progress[it.id] == null },
            dueWords = lessonWords.count { Leitner.isDue(progress[it.id], todayEpochDay) },
            learnedWords = states.count { (it?.correctCount ?: 0) > 0 },
            unlocked = isUnlocked(lesson, words, progress)
        )
    }

    /**
     * The lesson the user is on: the first open lesson that still has **unread** words,
     * because new words have priority. Once every lesson has been read at least once,
     * the first open lesson with due reviews wins. Reviews of earlier lessons are
     * offered as «مانده از قبل» and never block the lesson itself.
     */
    fun currentLesson(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        todayEpochDay: Long
    ): Int? {
        val overview = overview(words, progress, todayEpochDay)
        return overview.firstOrNull { it.unlocked && it.newWords > 0 }?.lesson
            ?: overview.firstOrNull { it.unlocked && it.dueWords > 0 }?.lesson
            ?: overview.lastOrNull { it.unlocked }?.lesson
    }
}
