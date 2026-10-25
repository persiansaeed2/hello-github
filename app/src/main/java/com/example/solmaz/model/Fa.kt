package com.example.solmaz.model

/** Persian digit / wording helpers shared by the whole UI. */

private val FA_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = buildString(length) {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') FA_DIGITS[ch - '0'] else ch)
    }
}

val Int.fa: String get() = toString().toPersianDigits()

/** ۰ → «۰ دقیقه»، ۷۵ → «۱ ساعت و ۱۵ دقیقه» */
fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "۰ دقیقه"
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "${rest.fa} دقیقه"
        rest == 0 -> "${hours.fa} ساعت"
        else -> "${hours.fa} ساعت و ${rest.fa} دقیقه"
    }
}

/** ۹۰ → «۱:۳۰» (برای نمایش روی تایمر/آمار) */
fun formatClock(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return "${hours.fa}:${rest.toString().padStart(2, '0').toPersianDigits()}"
}
