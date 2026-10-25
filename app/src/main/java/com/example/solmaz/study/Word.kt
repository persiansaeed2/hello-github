package com.example.solmaz.study

/**
 * A single vocabulary entry coming from the book, grouped by its lesson (درس).
 *
 * [id] must be stable across app restarts because progress is stored against it.
 */
data class Word(
    val id: String,
    val term: String,
    val meaning: String,
    val lesson: Int,
    /** Position of the word inside the book, so new words are introduced in order. */
    val order: Int = 0
)
