package com.example.solmaz.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Pronunciation of a word; abstracted so the UI stays testable. */
interface Speaker {
    fun speak(text: String)
    fun shutdown()
}

/**
 * German text-to-speech (the vocabulary book is German → Persian).
 * Everything is wrapped in runCatching: a phone without a TTS engine must never
 * break the study screen.
 */
class AndroidSpeaker(context: Context) : Speaker {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        runCatching {
            tts = TextToSpeech(context.applicationContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    runCatching { tts?.language = Locale.GERMAN }
                }
            }
        }
    }

    override fun speak(text: String) {
        if (!ready || text.isBlank()) return
        runCatching { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "solmaz-word") }
    }

    override fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
        ready = false
    }
}
