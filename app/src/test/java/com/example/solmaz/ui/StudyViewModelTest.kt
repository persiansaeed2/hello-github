package com.example.solmaz.ui

import com.example.solmaz.data.StudyStore
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.JalaliDate
import com.example.solmaz.model.StudySession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** In-memory store so the ViewModel can be exercised without Android. */
private class FakeStore(initial: List<StudySession> = emptyList()) : StudyStore {
    var stored: MutableList<StudySession> = initial.toMutableList()
    var saveCount = 0

    override fun load(): List<StudySession> = stored.toList()

    override fun save(sessions: List<StudySession>) {
        stored = sessions.toMutableList()
        saveCount++
    }
}

class StudyViewModelTest {

    private lateinit var store: FakeStore
    private lateinit var vm: StudyViewModel
    private val todayEpochDay get() = vm.today.epochDay

    @Before
    fun setUp() {
        store = FakeStore()
        vm = StudyViewModel(store)
    }

    @Test
    fun `adds a session, persists it and makes it appear today`() {
        vm.addSession(todayEpochDay, "ریاضی", 45)

        assertEquals(1, vm.sessions.size)
        assertEquals(45, vm.todayMinutes)
        assertEquals(45, vm.minutesOn(todayEpochDay))
        assertEquals(1, vm.sessionsOn(todayEpochDay).size)
        assertEquals("ریاضی", vm.sessionsOn(todayEpochDay).first().subject)
        assertEquals(1, store.saveCount)
        assertEquals(1, store.stored.size)
    }

    @Test
    fun `blank subject falls back to the default name`() {
        vm.addSession(todayEpochDay, "   ", 30)
        assertEquals("مطالعه", vm.sessions.first().subject)
    }

    @Test
    fun `non positive durations are ignored`() {
        vm.addSession(todayEpochDay, "فیزیک", 0)
        vm.addSession(todayEpochDay, "فیزیک", -20)
        assertTrue(vm.sessions.isEmpty())
        assertEquals(0, store.saveCount)
    }

    @Test
    fun `deleting removes the session from memory and storage`() {
        vm.addSession(todayEpochDay, "شیمی", 60)
        val id = vm.sessions.first().id
        vm.deleteSession(id)

        assertTrue(vm.sessions.isEmpty())
        assertEquals(0, vm.todayMinutes)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun `ids keep increasing so rows stay stable`() {
        vm.addSession(todayEpochDay, "a", 10)
        vm.addSession(todayEpochDay, "b", 20)
        val ids = vm.sessions.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
        assertTrue(ids.max() > ids.min())
    }

    @Test
    fun `week stats cover exactly seven days ending today`() {
        vm.addSession(todayEpochDay, "ریاضی", 30)
        vm.addSession(todayEpochDay - 3, "ریاضی", 60)

        val week = vm.lastSevenDays()
        assertEquals(7, week.size)
        assertEquals(todayEpochDay, week.last().date.epochDay)
        assertEquals(30, week.last().minutes)
        assertEquals(60, week.first { it.date.epochDay == todayEpochDay - 3 }.minutes)
    }

    @Test
    fun `streak counts consecutive studied days`() {
        vm.addSession(todayEpochDay, "ریاضی", 20)
        vm.addSession(todayEpochDay - 1, "ریاضی", 20)
        vm.addSession(todayEpochDay - 2, "ریاضی", 20)
        vm.addSession(todayEpochDay - 4, "ریاضی", 20)

        assertEquals(3, vm.streakDays())
    }

    @Test
    fun `streak still counts yesterday when today is empty`() {
        vm.addSession(todayEpochDay - 1, "ریاضی", 20)
        vm.addSession(todayEpochDay - 2, "ریاضی", 20)

        assertEquals(2, vm.streakDays())
    }

    @Test
    fun `totals and averages use every session`() {
        vm.addSession(todayEpochDay, "ریاضی", 60)
        vm.addSession(todayEpochDay - 1, "فیزیک", 30)

        assertEquals(90, vm.totalMinutes)
        assertEquals(2, vm.totalDays)
        assertEquals("فیزیک", vm.subjectStats().last().subject)
        assertEquals(60, vm.subjectStats().first().minutes)
        assertEquals(todayEpochDay, vm.bestDay()?.date?.epochDay)
    }

    @Test
    fun `best day is null without data`() {
        assertNull(vm.bestDay())
        assertEquals(0, vm.totalMinutes)
        assertEquals(0, vm.streakDays())
    }

    @Test
    fun `shifting months keeps the visible date valid in both directions`() {
        val start = JalaliCalendar.today()
        vm.shiftMonth(1 - start.month) // jump to Farvardin of the same year
        assertEquals(1, vm.visibleMonth)
        assertEquals(start.year, vm.visibleYear)

        vm.shiftMonth(-1) // Farvardin -> Esfand of the previous year
        assertEquals(12, vm.visibleMonth)
        assertEquals(start.year - 1, vm.visibleYear)

        vm.shiftMonth(1) // back to Farvardin
        assertEquals(1, vm.visibleMonth)
        assertEquals(start.year, vm.visibleYear)

        // The selected day follows the visible month and stays inside it.
        assertEquals(1, vm.selectedDate.month)
        assertEquals(1, vm.selectedDate.day)
    }

    @Test
    fun `goToMonthOf selects the day it is given`() {
        val target = JalaliCalendar.today().plusDays(-40)
        vm.goToMonthOf(target.epochDay)

        assertEquals(target.year, vm.visibleYear)
        assertEquals(target.month, vm.visibleMonth)
        assertEquals(target.epochDay, vm.selectedEpochDay)
    }

    @Test
    fun `monthly totals only count the requested month`() {
        val today = JalaliCalendar.today()
        vm.addSession(today.epochDay, "ریاضی", 40)
        val lastMonth = JalaliDate(today.year, if (today.month == 1) 12 else today.month - 1, 1)
        vm.addSession(lastMonth.epochDay, "ریاضی", 90)

        assertEquals(40, vm.minutesInMonth(today.year, today.month))
        assertEquals(90, vm.minutesInMonth(lastMonth.year, lastMonth.month))
        assertEquals(1, vm.studiedDaysInMonth(today.year, today.month))
    }
}
