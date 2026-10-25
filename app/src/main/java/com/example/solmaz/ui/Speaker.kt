package com.example.solmaz.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Which language to read a text in. */
enum class SpeechLang { GERMAN, PERSIAN }

/** Pronunciation of a word; abstracted so the UI stays testable. */
interface Speaker {
    fun speak(text: String, lang: SpeechLang = SpeechLang.GERMAN)
    fun shutdown()
}

/**
 * German text-to-speech (the vocabulary book is German → Persian).
 * Everything is wrapped in runCatching: a phone without a TTS engine must never
 * break the study screen.
 */
class AndroidSpeaker(private val context: Context) : Speaker {

    private class Engine(var tts: TextToSpeech? = null, var ready: Boolean = false)

    private val german = Engine()
    private val persian = Engine()

    private fun engineFor(lang: SpeechLang): Engine = if (lang == SpeechLang.GERMAN) german else persian

    private fun ensureStarted(lang: SpeechLang): Engine {
        val engine = engineFor(lang)
        if (engine.tts != null) return engine

        runCatching {
            engine.tts = TextToSpeech(context.applicationContext) { status ->
                engine.ready = status == TextToSpeech.SUCCESS
                if (engine.ready) {
                    val locale = when (lang) {
                        SpeechLang.GERMAN -> Locale.GERMAN
                        // Persian is offline as soon as the device has the voice data;
                        // isLanguageAvailable() tells us whether speaking is worth trying.
                        SpeechLang.PERSIAN -> Locale("fa", "IR")
                    }
                    val available = runCatching {
                        engine.tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    }.getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
                    engine.ready = available >= TextToSpeech.LANG_AVAILABLE
                    if (engine.ready) runCatching { engine.tts?.language = locale }
                }
            }
        }
        return engine
    }

    override fun speak(text: String, lang: SpeechLang) {
        if (text.isBlank()) return
        val engine = ensureStarted(lang)
        if (!engine.ready) return
        val locale = if (lang == SpeechLang.GERMAN) Locale.GERMAN else Locale("fa", "IR")
        runCatching {
            runCatching { engine.tts?.language = locale }
            engine.tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "solmaz-$lang")
        }
    }

    override fun shutdown() {
        listOf(german, persian).forEach { engine ->
            runCatching {
                engine.tts?.stop()
                engine.tts?.shutdown()
            }
            engine.tts = null
            engine.ready = false
        }
    }
}
