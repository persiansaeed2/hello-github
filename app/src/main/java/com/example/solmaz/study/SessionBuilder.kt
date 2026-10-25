package com.example.solmaz.study

/**
 * Builds the list of words for one practice round.
 *
 * Order (confirmed with the product owner):
 * 1. **new words of the current lesson first** — the lesson the user is on is the priority,
 * 2. then the due words of the current lesson,
 * 3. then the leftovers: due words from lessons already started
 *    («اگر وقت و حوصله داری، این‌ها از قبل مانده»),
 * 4. a round is 10 words by default.
 *
 * Because new words always come first, a word can never stay unread: every word of a
 * lesson is introduced before that lesson's reviews start repeating.
 */
object SessionBuilder {

    const val DEFAULT_SESSION_SIZE = 10

    fun nextSession(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        lesson: Int,
        todayEpochDay: Long,
        size: Int = DEFAULT_SESSION_SIZE,
        includeEarlierLessons: Boolean = true
    ): List<Word> {
        val currentLessonWords = words.filter { it.lesson == lesson }

        val fresh = currentLessonWords
            .filter { progress[it.id] == null }
            .sortedBy { it.order }

        val dueCurrent = dueOf(currentLessonWords, progress, todayEpochDay)

        val dueEarlier = if (includeEarlierLessons) {
            dueLeftovers(words, progress, lesson, todayEpochDay)
        } else {
            emptyList()
        }

        return (fresh + dueCurrent + dueEarlier).take(size)
    }

    /** Due words from earlier lessons: the «مانده از قبل» section of the today screen. */
    fun dueLeftovers(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        lesson: Int,
        todayEpochDay: Long
    ): List<Word> = dueOf(
        words = words.filter { it.lesson < lesson },
        progress = progress,
        todayEpochDay = todayEpochDay
    )

    /** Words of [words] whose review day has arrived (new words are not included). */
    fun dueOf(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        todayEpochDay: Long
    ): List<Word> = words
        .mapNotNull { word ->
            val state = progress[word.id] ?: return@mapNotNull null
            if (state.isNew || !Leitner.isDue(state, todayEpochDay)) {
                null
            } else {
                word to state
            }
        }
        .sortedWith(compareBy({ it.second.dueEpochDay ?: Long.MIN_VALUE }, { it.first.order }))
        .map { it.first }

    /** How many words are waiting right now (new + due, current lesson and leftovers). */
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
