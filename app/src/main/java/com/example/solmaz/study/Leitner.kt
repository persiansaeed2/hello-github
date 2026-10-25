package com.example.solmaz.study

/**
 * The Leitner rule itself: eight boxes with review intervals of
 * 1, 2, 4, 8, 16, 32, 64 and 128 days.
 *
 * * every word starts in box 1 (so it is shown as soon as the user opens a lesson)
 * * "I know it" → the word moves one box up, and comes back after that box's interval
 * * "I don't know it" → the word drops back to box 1 and comes back tomorrow
 *
 * All dates are epoch-days (days since 1970-01-01) so the logic stays timezone-proof
 * and trivially testable.
 */
object Leitner {

    const val FIRST_BOX = 1
    const val MAX_BOX = 8

    private val INTERVALS_DAYS = intArrayOf(1, 2, 4, 8, 16, 32, 64, 128)

    /** Review interval of a box, in days. Clamped so bad data can never crash the app. */
    fun intervalDays(box: Int): Int = INTERVALS_DAYS[box.coerceIn(FIRST_BOX, MAX_BOX) - 1]

    /** A word with no progress at all is a new word, hence due today. */
    fun isDue(progress: WordProgress?, todayEpochDay: Long): Boolean {
        if (progress == null) return true
        val due = progress.dueEpochDay ?: return true
        return due <= todayEpochDay
    }

    /** 🟢 the user knows the word: one box up, next review after the new box's interval. */
    fun onKnown(wordId: String, progress: WordProgress?, todayEpochDay: Long): WordProgress {
        val currentBox = progress?.box ?: FIRST_BOX
        val nextBox = (currentBox + 1).coerceAtMost(MAX_BOX)
        return WordProgress(
            wordId = wordId,
            box = nextBox,
            dueEpochDay = todayEpochDay + intervalDays(nextBox),
            lastReviewedEpochDay = todayEpochDay,
            correctCount = (progress?.correctCount ?: 0) + 1,
            wrongCount = progress?.wrongCount ?: 0
        )
    }

    /** 🔴 the user does not know the word: back to box 1, reviewed again tomorrow. */
    fun onUnknown(wordId: String, progress: WordProgress?, todayEpochDay: Long): WordProgress {
        return WordProgress(
            wordId = wordId,
            box = FIRST_BOX,
            dueEpochDay = todayEpochDay + intervalDays(FIRST_BOX),
            lastReviewedEpochDay = todayEpochDay,
            correctCount = progress?.correctCount ?: 0,
            wrongCount = (progress?.wrongCount ?: 0) + 1
        )
    }
}
