package com.example.solmaz.study

/**
 * Builds the list of words for one practice round.
 *
 * Rules (confirmed with the product owner):
 * 1. a round only ever contains words of the chosen lesson — lessons are never mixed,
 * 2. words whose review date has arrived come first (oldest due date first),
 * 3. the remaining slots are filled with brand-new words, in book order,
 * 4. a round is 10 words by default.
 */
object SessionBuilder {

    const val DEFAULT_SESSION_SIZE = 10

    fun nextSession(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        lesson: Int,
        todayEpochDay: Long,
        size: Int = DEFAULT_SESSION_SIZE
    ): List<Word> {
        val lessonWords = words.filter { it.lesson == lesson }.sortedBy { it.order }

        val due = lessonWords
            .mapNotNull { word ->
                val state = progress[word.id] ?: return@mapNotNull null
                if (!Leitner.isDue(state, todayEpochDay)) null else word to state
            }
            .sortedWith(compareBy({ it.second.dueEpochDay ?: Long.MIN_VALUE }, { it.first.order }))
            .map { it.first }

        val fresh = lessonWords.filter { progress[it.id] == null }

        return (due + fresh).take(size)
    }

    /** How many words of a lesson are waiting right now (due reviews + never-seen words). */
    fun pendingCount(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        lesson: Int,
        todayEpochDay: Long
    ): Int = nextSession(
        words = words,
        progress = progress,
        lesson = lesson,
        todayEpochDay = todayEpochDay,
        size = Int.MAX_VALUE
    ).size
}
