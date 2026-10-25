package com.example.motalee.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.motalee.data.StudyRepository
import com.example.motalee.model.JalaliCalendar
import com.example.motalee.model.JalaliDate
import com.example.motalee.model.StudySession

/** A day plus how long it was studied — used by the charts. */
data class DayStat(val date: JalaliDate, val minutes: Int)

/** One subject and its accumulated minutes — used by the stats screen. */
data class SubjectStat(val subject: String, val minutes: Int)

/**
 * Single source of truth for the UI: the list of sessions plus the currently
 * selected day / visible month of the Jalali calendar.
 */
class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    var sessions by mutableStateOf(repository.load().sortedByDescending { it.id })
        private set

    val today: JalaliDate = JalaliCalendar.today()

    var selectedEpochDay by mutableLongStateOf(today.epochDay)
        private set

    var visibleYear by mutableIntStateOf(today.year)
        private set

    var visibleMonth by mutableIntStateOf(today.month)
        private set

    val selectedDate: JalaliDate get() = JalaliCalendar.fromEpochDay(selectedEpochDay)

    // ------------------------------------------------------------------ queries

    fun minutesOn(epochDay: Long): Int =
        sessions.sumOf { if (it.epochDay == epochDay) it.minutes else 0 }

    fun sessionsOn(epochDay: Long): List<StudySession> =
        sessions.filter { it.epochDay == epochDay }

    val todayMinutes: Int get() = minutesOn(today.epochDay)

    val totalMinutes: Int get() = sessions.sumOf { it.minutes }

    val totalDays: Int get() = sessions.map { it.epochDay }.toSet().size

    /** Minutes per day for the last seven days, oldest first. */
    fun lastSevenDays(): List<DayStat> = (6 downTo 0).map { back ->
        val date = today.plusDays(-back.toLong())
        DayStat(date, minutesOn(date.epochDay))
    }

    /** Consecutive studied days ending today (today may still be empty). */
    fun streakDays(): Int {
        var day = today.epochDay
        if (minutesOn(day) == 0) day -= 1
        var streak = 0
        while (minutesOn(day) > 0) {
            streak++
            day--
        }
        return streak
    }

    fun subjectStats(): List<SubjectStat> = sessions
        .groupBy { it.subject }
        .map { (subject, items) -> SubjectStat(subject, items.sumOf { it.minutes }) }
        .sortedByDescending { it.minutes }

    fun bestDay(): DayStat? = sessions
        .groupBy { it.epochDay }
        .map { (day, items) -> DayStat(JalaliCalendar.fromEpochDay(day), items.sumOf { it.minutes }) }
        .maxByOrNull { it.minutes }

    fun minutesInMonth(year: Int, month: Int): Int = sessions.sumOf {
        val date = JalaliCalendar.fromEpochDay(it.epochDay)
        if (date.year == year && date.month == month) it.minutes else 0
    }

    fun studiedDaysInMonth(year: Int, month: Int): Int = sessions
        .filter {
            val date = JalaliCalendar.fromEpochDay(it.epochDay)
            date.year == year && date.month == month
        }
        .map { it.epochDay }
        .toSet()
        .size

    // ----------------------------------------------------------------- commands

    fun addSession(epochDay: Long, subject: String, minutes: Int) {
        if (minutes <= 0) return
        val clean = subject.trim().ifBlank { "مطالعه" }
        val nextId = (sessions.maxOfOrNull { it.id } ?: 0L) + 1L
        sessions = (sessions + StudySession(nextId, epochDay, clean, minutes))
            .sortedByDescending { it.id }
        repository.save(sessions)
    }

    fun deleteSession(id: Long) {
        sessions = sessions.filterNot { it.id == id }
        repository.save(sessions)
    }

    fun selectDay(epochDay: Long) {
        selectedEpochDay = epochDay
    }

    /** Moves the visible month; the selected day follows so both screens stay in sync. */
    fun shiftMonth(offset: Int) {
        var year = visibleYear
        var month = visibleMonth + offset
        if (month > 12) {
            month = 1
            year += 1
        } else if (month < 1) {
            month = 12
            year -= 1
        }
        visibleYear = year
        visibleMonth = month
        selectedEpochDay = JalaliDate(year, month, 1).epochDay
    }

    fun goToMonthOf(epochDay: Long) {
        val date = JalaliCalendar.fromEpochDay(epochDay)
        visibleYear = date.year
        visibleMonth = date.month
        selectedEpochDay = epochDay
    }

    companion object {
        const val DAILY_GOAL_MINUTES = 120
    }
}

/** Manual factory: the screen owns the repository, no DI framework needed yet. */
class StudyViewModelFactory(
    private val repository: StudyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return StudyViewModel(repository) as T
    }
}
