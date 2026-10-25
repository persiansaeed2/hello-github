package com.example.solmaz.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTextTest {
    @Test fun abbreviationsAreSpelledOut() {
        assertEquals("jemanden anstecken", SpeechText.german("jdn. anstecken"))
        assertEquals("etwas auswendig lernen", SpeechText.german("etw. auswendig lernen"))
        assertEquals("jemandem Geld schulden", SpeechText.german("jdm. Geld schulden"))
    }

    @Test fun markersAreDropped() {
        assertEquals("die Klasse besuchen", SpeechText.german("die ... Klasse besuchen"))
        assertEquals("sich Zeit lassen", SpeechText.german("sich (D) Zeit lassen"))
        assertEquals("einwandern", SpeechText.german("einwandern*"))
    }

    @Test fun initialsAreSpelledLetterByLetter() {
        assertEquals("der Numerus clausus N C", SpeechText.german("der Numerus clausus (N.C.)"))
    }

    @Test fun slashBecomesPause() {
        assertEquals("beschließen, verabschieden", SpeechText.german("beschließen / verabschieden"))
    }
}
