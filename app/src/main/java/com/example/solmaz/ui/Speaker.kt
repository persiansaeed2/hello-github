package com.example.solmaz.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
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
 * Text-to-speech for German (the vocabulary book is German → Persian) and Persian.
 *
 * - The book's abbreviations ("etw.", "jdn.", "(D)", "...") are spelled out first, see [SpeechText].
 * - The engine starts asynchronously: a tap that arrives while it is still starting is remembered
 *   and spoken as soon as the engine is ready (before, the first tap was silently lost).
 * - If the phone has no voice data for the language, [onUnavailable] is called on the main thread
 *   so the UI can tell the user instead of staying silent.
 * Everything is wrapped in runCatching: a phone without a TTS engine must never break the study screen.
 */
class AndroidSpeaker(
    private val context: Context,
    private val onUnavailable: (SpeechLang) -> Unit = {}
) : Speaker {

    private class Engine(
        var tts: TextToSpeech? = null,
        var ready: Boolean = false,
        var failed: Boolean = false,
        var pending: String? = null
    )

    private val main = Handler(Looper.getMainLooper())
    private val german = Engine()
    private val persian = Engine()

    private fun engineFor(lang: SpeechLang): Engine = if (lang == SpeechLang.GERMAN) german else persian

    private fun localeFor(lang: SpeechLang): Locale =
        if (lang == SpeechLang.GERMAN) Locale.GERMAN else Locale("fa", "IR")

    private fun notifyUnavailable(lang: SpeechLang) {
        main.post { runCatching { onUnavailable(lang) } }
    }

    private fun release(engine: Engine) {
        runCatching {
            engine.tts?.stop()
            engine.tts?.shutdown()
        }
        engine.tts = null
        engine.ready = false
        engine.failed = false
    }

    private fun ensureStarted(lang: SpeechLang): Engine {
        val engine = engineFor(lang)
        if (engine.tts != null) return engine

        val started = runCatching {
            engine.tts = TextToSpeech(context.applicationContext) { status ->
                val locale = localeFor(lang)
                var ok = status == TextToSpeech.SUCCESS
                if (ok) {
                    val available = runCatching {
                        engine.tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    }.getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
                    ok = available >= TextToSpeech.LANG_AVAILABLE
                }
                engine.ready = ok
                engine.failed = !ok
                if (ok) {
                    runCatching { engine.tts?.language = locale }
                    if (lang == SpeechLang.GERMAN) runCatching { engine.tts?.setSpeechRate(0.9f) }
                    engine.pending?.let { say(engine, lang, it) }
                } else {
                    notifyUnavailable(lang)
                }
                engine.pending = null
            }
        }.isSuccess
        if (!started) {
            engine.failed = true
            notifyUnavailable(lang)
        }
        return engine
    }

    private fun say(engine: Engine, lang: SpeechLang, text: String) {
        val spoken = if (lang == SpeechLang.GERMAN) SpeechText.german(text) else text
        if (spoken.isBlank()) return
        runCatching {
            engine.tts?.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "solmaz-$lang")
        }
    }

    override fun speak(text: String, lang: SpeechLang) {
        if (text.isBlank()) return
        var engine = ensureStarted(lang)
        if (engine.failed) {
            // Retry: the user may have installed the voice data since the last attempt.
            release(engine)
            engine = ensureStarted(lang)
        }
        if (engine.ready) say(engine, lang, text) else engine.pending = text
    }

    override fun shutdown() {
        listOf(german, persian).forEach { engine ->
            release(engine)
            engine.pending = null
        }
    }
}
