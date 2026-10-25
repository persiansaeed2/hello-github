package com.example.solmaz.data

import android.content.Context
import com.example.solmaz.study.Word
import com.example.solmaz.study.WordJsonParser

/** Words plus where they came from, so the UI can explain what it is showing. */
data class WordLoadResult(
    val words: List<Word>,
    val source: String,
    val skipped: Int = 0,
    val error: String? = null
) {
    val isEmpty: Boolean get() = words.isEmpty()
}

/**
 * The one place the app reads vocabulary from.
 *
 * ```
 * app/src/main/assets/words.json         ← the real export goes here
 * app/src/main/assets/words.sample.json  ← demo data, used until the real file exists
 * ```
 *
 * No storage permission is needed and it works fully offline: the JSON is packed
 * into the APK at build time. Replacing the file only requires rebuilding the app.
 */
class WordSource(private val context: Context) {

    fun load(): WordLoadResult {
        readAsset(REAL_FILE)?.let { raw ->
            return when (val parsed = WordJsonParser.parse(raw)) {
                is WordJsonParser.Result.Success ->
                    WordLoadResult(parsed.words, REAL_FILE, parsed.skipped)
                is WordJsonParser.Result.Failure ->
                    WordLoadResult(emptyList(), REAL_FILE, error = parsed.message)
            }
        }

        readAsset(SAMPLE_FILE)?.let { raw ->
            return when (val parsed = WordJsonParser.parse(raw)) {
                is WordJsonParser.Result.Success ->
                    WordLoadResult(parsed.words, SAMPLE_FILE, parsed.skipped)
                is WordJsonParser.Result.Failure ->
                    WordLoadResult(emptyList(), SAMPLE_FILE, error = parsed.message)
            }
        }

        return WordLoadResult(
            words = emptyList(),
            source = REAL_FILE,
            error = "فایل لغات پیدا نشد؛ آن را در assets/$REAL_FILE بگذار."
        )
    }

    private fun readAsset(name: String): String? = runCatching {
        context.assets.open(name).bufferedReader().use { it.readText() }
    }.getOrNull()

    companion object {
        const val REAL_FILE = "words.json"
        const val SAMPLE_FILE = "words.sample.json"
        const val ASSET_DIR = "app/src/main/assets"
    }
}
