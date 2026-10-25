package com.example.solmaz.study

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric because the parser uses org.json (real implementation
 * instead of the stubbed android.jar used by plain JVM tests).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class WordJsonParserTest {

    private fun success(json: String): WordJsonParser.Result.Success =
        WordJsonParser.parse(json) as? WordJsonParser.Result.Success
            ?: error("expected success but got ${WordJsonParser.parse(json)}")

    @Test
    fun `flat array with english keys`() {
        val result = success(
            """[
              {"word":"apple","meaning":"سیب","lesson":2},
              {"word":"book","meaning":"کتاب","lesson":2}
            ]"""
        )

        assertEquals(2, result.words.size)
        assertEquals(0, result.skipped)
        assertEquals(Word("L2:apple", "apple", "سیب", 2, 0), result.words[0])
        assertEquals(2, result.words[1].lesson)
    }

    @Test
    fun `accepts alias keys and string lesson numbers`() {
        val result = success(
            """[
              {"Term":"apple","Translation":"سیب","unit":"3"},
              {"لغت":"book","معنی":"کتاب","درس":3}
            ]"""
        )

        assertEquals(2, result.words.size)
        assertEquals(listOf(3, 3), result.words.map { it.lesson })
        assertEquals("apple", result.words[0].term)
        assertEquals("کتاب", result.words[1].meaning)
    }

    @Test
    fun `accepts german column names`() {
        val result = success(
            """[
              {"Wort":"Apfel","Übersetzung":"سیب","Lektion":2},
              {"Wort":"Buch","Bedeutung":"کتاب","Kapitel":"2"}
            ]"""
        )

        assertEquals(2, result.words.size)
        assertEquals(listOf(2, 2), result.words.map { it.lesson })
        assertEquals("Apfel", result.words[0].term)
        assertEquals("کتاب", result.words[1].meaning)
    }

    @Test
    fun `wrapped in a words key`() {
        val result = success("""{"words":[{"word":"a","meaning":"الف","lesson":1}]}""")
        assertEquals(1, result.words.size)
    }

    @Test
    fun `lessons array carries the lesson number`() {
        val result = success(
            """{"lessons":[
                 {"number":1,"words":[{"word":"a","meaning":"الف"},{"word":"b","meaning":"ب"}]},
                 {"number":2,"words":[{"word":"c","meaning":"پ"}]}
               ]}"""
        )

        assertEquals(3, result.words.size)
        assertEquals(listOf(1, 1, 2), result.words.map { it.lesson })
    }

    @Test
    fun `lessons object keyed by lesson number`() {
        val result = success(
            """{"lessons":{"1":[{"word":"a","meaning":"الف"}],"5":[{"word":"b","meaning":"ب"}]}}"""
        )

        assertEquals(listOf(1, 5), result.words.map { it.lesson })
    }

    @Test
    fun `broken entries are skipped and reported`() {
        val result = success(
            """[
              {"word":"ok","meaning":"درست"},
              {"word":"","meaning":"بی‌کلمه"},
              {"meaning":"بی‌لغت"},
              {"word":"ok2","meaning":"درست۲"}
            ]"""
        )

        assertEquals(2, result.words.size)
        assertEquals(2, result.skipped)
    }

    @Test
    fun `explicit ids are respected`() {
        val result = success("""[{"id":"custom-1","word":"a","meaning":"الف","lesson":1}]""")
        assertEquals("custom-1", result.words.first().id)
    }

    @Test
    fun `invalid payloads fail with a message instead of crashing`() {
        assertTrue(WordJsonParser.parse("") is WordJsonParser.Result.Failure)
        assertTrue(WordJsonParser.parse("   ") is WordJsonParser.Result.Failure)
        assertTrue(WordJsonParser.parse("not json") is WordJsonParser.Result.Failure)
        assertTrue(WordJsonParser.parse("""[{"word":"a"}""") is WordJsonParser.Result.Failure)
        assertTrue(WordJsonParser.parse("""{"unknown":true}""") is WordJsonParser.Result.Failure)
        assertTrue(WordJsonParser.parse("""[]""") is WordJsonParser.Result.Failure)
    }

    @Test
    fun `bom and whitespace are tolerated`() {
        val result = success("\uFEFF\n  [ {\"word\":\"a\",\"meaning\":\"الف\"} ]\n")
        assertEquals(1, result.words.size)
    }
}
