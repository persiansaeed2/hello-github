package com.example.solmaz.model

/** One study block: how many minutes were studied on which day for which subject. */
data class StudySession(
    val id: Long,
    val epochDay: Long,
    val subject: String,
    val minutes: Int
) {
    val date: JalaliDate get() = JalaliCalendar.fromEpochDay(epochDay)
}
