package com.example.solmaz.study

/**
 * Leitner state of one word.
 *
 * [dueEpochDay] is null for a word that has never been reviewed: those words are
 * considered due immediately (they are the "new" words of a lesson).
 */
data class WordProgress(
    val wordId: String,
    val box: Int = Leitner.FIRST_BOX,
    val dueEpochDay: Long? = null,
    val lastReviewedEpochDay: Long? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
) {
    val isNew: Boolean get() = lastReviewedEpochDay == null
}
