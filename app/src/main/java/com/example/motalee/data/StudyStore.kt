package com.example.motalee.data

import com.example.motalee.model.StudySession

/**
 * Storage abstraction for study sessions.
 *
 * The UI only ever talks to this interface, which keeps [com.example.motalee.ui.StudyViewModel]
 * testable on the JVM (in-memory fake) and makes swapping SharedPreferences for Room a
 * one-line change.
 */
interface StudyStore {
    fun load(): List<StudySession>
    fun save(sessions: List<StudySession>)
}
