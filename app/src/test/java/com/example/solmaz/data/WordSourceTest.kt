package com.example.solmaz.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.example.solmaz.study.SessionBuilder
import com.example.solmaz.study.WordJsonParser
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
    fun `the real words file is loaded`() {
        val result = WordSource(context).load()

        assertNull(result.error)
        assertEquals(WordSource.REAL_FILE, result.source)
        assertTrue("expected the whole book, got ${result.words.size}", result.words.size > 1000)
        assertTrue(result.words.all { it.term.isNotBlank() && it.meaning.isNotBlank() })
        assertEquals(1, result.words.first().lesson)
        assertTrue(result.words.any { it.unitTitle.isNotBlank() })
    }

    @Test
    fun `the sample asset still parses`() {
        val raw = context.assets.open(WordSource.SAMPLE_FILE).bufferedReader().use { it.readText() }
        val parsed = WordJsonParser.parse(raw)

        assertTrue(parsed is WordJsonParser.Result.Success)
        assertEquals(12, (parsed as WordJsonParser.Result.Success).words.size)
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
        val session = SessionBuilder.nextSession(
            words = words,
            progress = emptyMap(),
            lesson = 1,
            todayEpochDay = 20_000L
        )

        assertEquals(SessionBuilder.DEFAULT_SESSION_SIZE, session.size)
        assertTrue(session.all { it.lesson == 1 })
    }
}
