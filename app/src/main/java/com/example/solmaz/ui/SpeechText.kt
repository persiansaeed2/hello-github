package com.example.solmaz.ui

/**
 * Turns the book's notation into text a German voice reads correctly:
 * "etw." -> "etwas", "jdn." -> "jemanden", "(D)" and "..." are dropped, "a / b" becomes a short pause.
 * Pure Kotlin so it can be unit-tested on the JVM.
 */
object SpeechText {
    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("""\betw\.""") to "etwas",
        Regex("""\bjdn\.""") to "jemanden",
        Regex("""\bjdm\.""") to "jemandem",
        Regex("""\bjds\.""") to "jemandes",
        Regex("""\bjd\.""") to "jemand",
        Regex("""\bz\. ?B\.""") to "zum Beispiel",
        Regex("""\bd\. ?h\.""") to "das heißt",
        Regex("""\bu\. ?a\.""") to "unter anderem",
        Regex("""\busw\.""") to "und so weiter",
        Regex("""\(\s*(D|Gen\.|\+ ?Gen\.|\+ ?Dat\.|\+ ?Akk\.|Pl\.)\s*\)""") to " ",
        Regex("""\.\.\.|…""") to " ",
        Regex("""(?<=\b[A-ZÄÖÜ])\.""") to " ",  // "N.C." is spelled letter by letter
        Regex("""[()*!]""") to " ",
        Regex("""\s*/\s*""") to ", ",
    )

    fun german(raw: String): String {
        var s = raw
        for ((rule, replacement) in rules) s = rule.replace(s, replacement)
        return s.replace(Regex("""\s+"""), " ").replace(Regex("""\s+,"""), ",").trim(' ', ',')
    }
}
