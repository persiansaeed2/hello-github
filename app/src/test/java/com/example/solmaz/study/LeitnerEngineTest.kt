package com.example.solmaz.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeitnerEngineTest {

    private val today = 20_000L

    private fun word(id: String, lesson: Int, order: Int) =
        Word(id = id, term = "term-$id", meaning = "معنی-$id", lesson = lesson, order = order)

    // ---------------------------------------------------------------- boxes

    @Test
    fun `intervals are one two four up to one twenty eight days`() {
        val expected = listOf(1, 2, 4, 8, 16, 32, 64, 128)
        assertEquals(expected, (1..8).map { Leitner.intervalDays(it) })
        // out of range values are clamped instead of crashing
        assertEquals(1, Leitner.intervalDays(0))
        assertEquals(128, Leitner.intervalDays(99))
    }

    @Test
    fun `a brand new word is due today`() {
        assertTrue(Leitner.isDue(null, today))

        val fresh = WordProgress(wordId = "w1")
        assertTrue(fresh.isNew)
        assertTrue(Leitner.isDue(fresh, today))
    }

    @Test
    fun `knowing a word moves it one box up and schedules the new interval`() {
        var state = Leitner.onKnown("w1", null, today) // box 1 -> 2
        assertEquals(2, state.box)
        assertEquals(today + 2, state.dueEpochDay)
        assertEquals(1, state.correctCount)
        assertFalse(state.isNew)

        state = Leitner.onKnown("w1", state, today)
        assertEquals(3, state.box)
        assertEquals(today + 4, state.dueEpochDay)
    }

    @Test
    fun `the eighth box is the last one`() {
        val inLastBox = WordProgress(
            "w1",
            box = 8,
            dueEpochDay = today - 1,
            lastReviewedEpochDay = today - 129,
            correctCount = 7
        )
        val state = Leitner.onKnown("w1", inLastBox, today)

        assertEquals(8, state.box)
        assertEquals(today + 128, state.dueEpochDay)
    }

    @Test
    fun `not knowing a word sends it back to box one and due tomorrow`() {
        val advanced = WordProgress(
            "w1",
            box = 5,
            dueEpochDay = today - 1,
            lastReviewedEpochDay = today - 17,
            correctCount = 4
        )
        val state = Leitner.onUnknown("w1", advanced, today)

        assertEquals(1, state.box)
        assertEquals(today + 1, state.dueEpochDay)
        assertEquals(4, state.correctCount) // history is kept
        assertEquals(1, state.wrongCount)
    }

    @Test
    fun `review dates are respected`() {
        val dueTomorrow = WordProgress("w1", box = 1, dueEpochDay = today + 1, lastReviewedEpochDay = today)
        assertFalse(Leitner.isDue(dueTomorrow, today))
        assertTrue(Leitner.isDue(dueTomorrow, today + 1))
        assertTrue(Leitner.isDue(dueTomorrow, today + 5))
    }

    // ---------------------------------------------------------------- sessions

    @Test
    fun `a session only contains the chosen lesson`() {
        val words = (1..3).flatMap { lesson -> (1..5).map { word("l$lesson-w$it", lesson, it) } }

        val session = SessionBuilder.nextSession(words, emptyMap(), lesson = 2, todayEpochDay = today)

        assertEquals(5, session.size) // lesson 2 only has five words
        assertTrue(session.all { it.lesson == 2 })
    }

    @Test
    fun `a session is capped at ten words`() {
        val words = (1..50).map { word("w$it", 6, it) }
        val session = SessionBuilder.nextSession(words, emptyMap(), lesson = 6, todayEpochDay = today)

        assertEquals(10, session.size)
        // new words are introduced in book order
        assertEquals((1..10).map { "w$it" }, session.map { it.id })
    }

    @Test
    fun `due reviews come before new words`() {
        val words = (1..15).map { word("w$it", 1, it) }
        val progress = mapOf(
            "w12" to WordProgress("w12", box = 2, dueEpochDay = today - 3, lastReviewedEpochDay = today - 5, correctCount = 1),
            "w14" to WordProgress("w14", box = 1, dueEpochDay = today, lastReviewedEpochDay = today - 1, correctCount = 1),
            "w15" to WordProgress("w15", box = 3, dueEpochDay = today + 10, lastReviewedEpochDay = today, correctCount = 2)
        )

        val session = SessionBuilder.nextSession(words, progress, lesson = 1, todayEpochDay = today)

        assertEquals("w12", session[0].id) // oldest due first
        assertEquals("w14", session[1].id)
        assertFalse(session.any { it.id == "w15" }) // not due yet
        assertEquals(10, session.size)
        assertTrue(session.drop(2).all { it.id !in progress.keys })
    }

    @Test
    fun `an empty lesson produces an empty session`() {
        val words = listOf(word("w1", 1, 1))
        assertTrue(SessionBuilder.nextSession(words, emptyMap(), lesson = 9, todayEpochDay = today).isEmpty())
        assertEquals(0, SessionBuilder.pendingCount(words, emptyMap(), 9, today))
    }

    @Test
    fun `pending count includes new and due words`() {
        val words = (1..12).map { word("w$it", 1, it) }
        val progress = mapOf(
            "w1" to WordProgress("w1", box = 2, dueEpochDay = today, lastReviewedEpochDay = today - 2, correctCount = 1),
            "w2" to WordProgress("w2", box = 4, dueEpochDay = today + 30, lastReviewedEpochDay = today, correctCount = 3)
        )

        // 1 due (w1) + 12 new minus the 2 studied ones (w3..w12) = 11
        assertEquals(11, SessionBuilder.pendingCount(words, progress, 1, today))
    }

    // ---------------------------------------------------------------- lessons

    @Test
    fun `only the first lesson is open at the beginning`() {
        val words = (1..3).flatMap { lesson -> (1..4).map { word("l$lesson-w$it", lesson, it) } }

        assertTrue(LessonPlan.isUnlocked(1, words, emptyMap()))
        assertFalse(LessonPlan.isUnlocked(2, words, emptyMap()))
        assertFalse(LessonPlan.isUnlocked(3, words, emptyMap()))
    }

    @Test
    fun `next lesson opens after the previous one is fully answered correctly`() {
        val words = listOf(word("a", 1, 1), word("b", 1, 2), word("c", 2, 1))
        val partial = mapOf("a" to WordProgress("a", box = 2, correctCount = 1))
        assertFalse(LessonPlan.isUnlocked(2, words, partial))

        val complete = partial + ("b" to WordProgress("b", box = 2, correctCount = 1))
        assertTrue(LessonPlan.isUnlocked(2, words, complete))
    }

    @Test
    fun `lesson overview reports counts per lesson`() {
        val words = listOf(word("a", 1, 1), word("b", 1, 2), word("c", 2, 1))
        val progress = mapOf(
            "a" to WordProgress("a", box = 3, dueEpochDay = today + 9, lastReviewedEpochDay = today, correctCount = 2)
        )

        val overview = LessonPlan.overview(words, progress, today)

        assertEquals(listOf(1, 2), overview.map { it.lesson })
        assertEquals(2, overview[0].totalWords)
        assertEquals(1, overview[0].newWords)
        assertEquals(1, overview[0].learnedWords) // only "a" was answered correctly
        assertEquals(1, overview[0].dueWords)     // only "b" is due
        assertFalse(overview[1].unlocked)
    }

    @Test
    fun `current lesson skips finished lessons`() {
        val words = listOf(word("a", 1, 1), word("b", 2, 1))
        val progress = mapOf(
            "a" to WordProgress("a", box = 2, dueEpochDay = today + 5, lastReviewedEpochDay = today, correctCount = 1)
        )

        assertEquals(2, LessonPlan.currentLesson(words, progress, today))
        assertNull(LessonPlan.currentLesson(emptyList(), emptyMap(), today))
    }
}
