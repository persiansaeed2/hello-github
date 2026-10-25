package com.example.solmaz.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Verifies the bundled asset is picked up and parsed (uses the real Robolectric context). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class WordSourceTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `sample asset is loaded when the real file is absent`() {
        val result = WordSource(context).load()

        assertNull(result.error)
        assertEquals(WordSource.SAMPLE_FILE, result.source)
        assertEquals(12, result.words.size)
        assertEquals(listOf(1, 2), result.words.map { it.lesson }.distinct())
        assertTrue(result.words.all { it.term.isNotBlank() && it.meaning.isNotBlank() })
    }

    @Test
    fun `ids are stable between loads`() {
        val first = WordSource(context).load().words.map { it.id }
        val second = WordSource(context).load().words.map { it.id }
        assertEquals(first, second)
    }

    @Test
    fun `word list feeds the session builder`() {
        val words = WordSource(context).load().words
        val session = com.example.solmaz.study.SessionBuilder.nextSession(
            words = words,
            progress = emptyMap(),
            lesson = 1,
            todayEpochDay = 20_000L
        )

        assertEquals(6, session.size) // the sample lesson 1 has six words
        assertTrue(session.all { it.lesson == 1 })
    }
}
