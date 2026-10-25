package com.example.solmaz.ui

import com.example.solmaz.data.ProgressStore
import com.example.solmaz.study.Word
import com.example.solmaz.study.WordProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeProgressStore(initial: Map<String, WordProgress> = emptyMap()) : ProgressStore {
    var stored: Map<String, WordProgress> = initial
    var saveCount = 0

    override fun load(): Map<String, WordProgress> = stored

    override fun save(progress: Map<String, WordProgress>) {
        stored = progress
        saveCount++
    }
}

class TodayViewModelTest {

    private val today = 20_000L
    private val words = listOf(
        Word("a", "apple", "سیب", 1, 0),
        Word("b", "book", "کتاب", 1, 1),
        Word("c", "cat", "گربه", 1, 2),
        Word("d", "dog", "سگ", 2, 0),
        Word("e", "egg", "تخم‌مرغ", 2, 1)
    )

    private lateinit var store: FakeProgressStore
    private lateinit var vm: TodayViewModel

    @Before
    fun setUp() {
        store = FakeProgressStore()
        vm = TodayViewModel(words, store, todayEpochDay = today)
    }

    @Test
    fun `everything starts new in lesson one`() {
        assertEquals(1, vm.lesson)
        assertEquals(3, vm.items().size)
        assertTrue(vm.items().all { it.status == WordStatus.NEW })
        assertEquals(0, vm.count(WordStatus.DUE))
        assertEquals(3, vm.todayTotal)
    }

    @Test
    fun `a session contains the lesson words and reveals them one by one`() {
        vm.startSession()

        assertTrue(vm.sessionActive)
        assertEquals(3, vm.sessionTotal)
        assertEquals("apple", vm.currentWord?.term)
        assertFalse(vm.revealed)

        vm.reveal()
        assertTrue(vm.revealed)
    }

    @Test
    fun `knowing a word moves it to the next box and out of today`() {
        vm.startSession()
        vm.reveal()
        vm.answer(known = true)

        val saved = store.stored.getValue("a")
        assertEquals(2, saved.box)
        assertEquals(today + 2, saved.dueEpochDay)
        assertEquals(1, vm.answeredCorrect)
        assertEquals(1, vm.sessionIndex)

        val item = vm.items().first { it.word.id == "a" }
        assertEquals(WordStatus.LATER, item.status)
        assertEquals(today + 2, item.nextReviewDate?.epochDay)
    }

    @Test
    fun `an unknown word goes back to box one and returns in the same session`() {
        vm.startSession()
        vm.reveal()
        vm.answer(known = false)

        val saved = store.stored.getValue("a")
        assertEquals(1, saved.box)
        assertEquals(today + 1, saved.dueEpochDay)
        assertEquals(1, vm.answeredWrong)

        // it is queued again at the end of this session
        assertEquals(4, vm.sessionQueue.size)
        assertEquals("a", vm.sessionQueue.last().id)
        assertEquals(WordStatus.LATER, vm.items().first { it.word.id == "a" }.status)
    }

    @Test
    fun `the session ends after every card is answered`() {
        vm.startSession()
        repeat(3) {
            vm.reveal()
            vm.answer(known = true)
        }

        assertFalse(vm.sessionActive)
        assertEquals(3, vm.answeredCorrect)
        assertEquals(0, vm.answeredWrong)
        // lesson 1 is done, so the screen has already moved on to lesson 2
        assertEquals(2, vm.lesson)
        assertEquals(2, vm.todayTotal)
    }

    @Test
    fun `progress is persisted on every answer`() {
        vm.startSession()
        vm.reveal()
        vm.answer(known = true)

        assertEquals(1, store.saveCount)
        assertNotNull(store.stored["a"])
    }

    @Test
    fun `the next lesson unlocks once lesson one is fully known`() {
        vm.startSession()
        repeat(3) {
            vm.reveal()
            vm.answer(known = true)
        }

        assertEquals(2, vm.lesson)
        assertEquals(listOf("d", "e"), vm.items().map { it.word.id })
    }

    @Test
    fun `a due word shows its review number and comes after the new words`() {
        store = FakeProgressStore(
            mapOf(
                "c" to WordProgress("c", box = 1, dueEpochDay = today, lastReviewedEpochDay = today - 1, correctCount = 1)
            )
        )
        vm = TodayViewModel(words, store, todayEpochDay = today)

        val items = vm.items()
        // new words of the current lesson keep the lead
        assertEquals(WordStatus.NEW, items.first().status)
        val due = items.first { it.word.id == "c" }
        assertEquals(WordStatus.DUE, due.status)
        assertEquals(2, due.reviewNumber) // «مرور ۲»
        assertEquals(1, vm.count(WordStatus.DUE))
    }

    @Test
    fun `leftovers from an earlier lesson are offered after the current lesson`() {
        store = FakeProgressStore(
            mapOf(
                "a" to WordProgress("a", box = 1, dueEpochDay = today - 1, lastReviewedEpochDay = today - 2, wrongCount = 1),
                "b" to WordProgress("b", box = 2, dueEpochDay = today - 1, lastReviewedEpochDay = today - 3, correctCount = 1),
                // read and scheduled in the future: lesson 1 is fully read, nothing due
                "c" to WordProgress("c", box = 3, dueEpochDay = today + 4, lastReviewedEpochDay = today - 1, correctCount = 2)
            )
        )
        // lesson 1 is fully read, so lesson 2 is the current one
        vm = TodayViewModel(words, store, todayEpochDay = today)

        assertEquals(2, vm.lesson)
        assertEquals(listOf("a", "b"), vm.leftovers().map { it.word.id })
        assertEquals(2, vm.leftovers().size)
        // current lesson (2 new) + 2 leftovers
        assertEquals(4, vm.todayTotal)

        vm.startSession()
        assertEquals(listOf("d", "e", "a", "b"), vm.sessionQueue.map { it.id })
    }

    @Test
    fun `empty word list is handled`() {
        val empty = TodayViewModel(emptyList(), FakeProgressStore(), today)
        assertEquals(0, empty.todayTotal)
        assertTrue(empty.items().isEmpty())
        empty.startSession()
        assertFalse(empty.sessionActive)
    }

    @Test
    fun `exiting a session returns to the list without touching progress`() {
        vm.startSession()
        vm.exitSession()

        assertFalse(vm.sessionActive)
        assertEquals(0, store.saveCount)
        assertEquals(3, vm.todayTotal)
    }
}
