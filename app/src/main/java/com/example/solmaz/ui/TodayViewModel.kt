package com.example.solmaz.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solmaz.data.ProgressStore
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.JalaliDate
import com.example.solmaz.study.Leitner
import com.example.solmaz.study.LessonPlan
import com.example.solmaz.study.SessionBuilder
import com.example.solmaz.study.Word
import com.example.solmaz.study.WordProgress
import java.time.LocalDate

/** Colour-coded state of a word on the today screen. */
enum class WordStatus {
    /** Never studied — nothing has been read yet. */
    NEW,

    /** Studied before, its review day has arrived. */
    DUE,

    /** Scheduled for a later day. */
    LATER
}

data class LessonBar(val lesson: Int, val read: Float, val strong: Float)

data class WordItem(
    val word: Word,
    val status: WordStatus,
    /** 1 = first review, 2 = second … shown as «مرور ۲». */
    val reviewNumber: Int,
    val nextReviewDate: JalaliDate?
)

/**
 * The single screen of the app: what to study today.
 *
 * Everything on screen is derived from the words file plus the stored Leitner state,
 * so changing one of them instantly changes the list.
 */
class TodayViewModel(
    private val words: List<Word>,
    private val store: ProgressStore,
    val todayEpochDay: Long = LocalDate.now().toEpochDay()
) : ViewModel() {

    var progress by mutableStateOf(store.load())
        private set

    val today: JalaliDate get() = JalaliCalendar.fromEpochDay(todayEpochDay)

    /** The lesson the user is on — lessons unlock one after another. */
    val lesson: Int? get() = LessonPlan.currentLesson(words, progress, todayEpochDay)

    /** Book title of the current lesson, e.g. «Schule und Bildung» (lesson 1.1). */
    val lessonTitle: String?
        get() = lesson?.let { number ->
            words.firstOrNull { it.lesson == number && it.unitTitle.isNotBlank() }
                ?.unitTitle
                // «1.1 Schule und Bildung» -> «Schule und Bildung»
                ?.substringAfter(' ', missingDelimiterValue = "")
                ?.takeIf { it.isNotBlank() }
        }

    // ------------------------------------------------------------------ list

    /** Every word of the current lesson with its colour-coded status. */
    fun items(): List<WordItem> {
        val currentLesson = lesson ?: return emptyList()
        return words
            .filter { it.lesson == currentLesson }
            .map { word -> item(word, progress[word.id]) }
            .sortedWith(
                compareBy(
                    { statusOrder(it.status) },
                    { it.nextReviewDate?.epochDay ?: Long.MIN_VALUE },
                    { it.word.order }
                )
            )
    }

    private fun item(word: Word, state: WordProgress?): WordItem {
        val status = when {
            state == null || state.isNew -> WordStatus.NEW
            Leitner.isDue(state, todayEpochDay) -> WordStatus.DUE
            else -> WordStatus.LATER
        }
        return WordItem(
            word = word,
            status = status,
            reviewNumber = (state?.correctCount ?: 0) + 1,
            nextReviewDate = state?.dueEpochDay?.let { JalaliCalendar.fromEpochDay(it) }
        )
    }

    /** New words first: the lesson the user is on always has priority. */
    private fun statusOrder(status: WordStatus): Int = when (status) {
        WordStatus.NEW -> 0
        WordStatus.DUE -> 1
        WordStatus.LATER -> 2
    }

    fun count(status: WordStatus): Int = items().count { it.status == status }

    /**
     * Due words from lessons the user already started — the «مانده از قبل» section.
     * They are offered after the current lesson, so nothing is left half-read.
     */
    fun leftovers(): List<WordItem> {
        val currentLesson = lesson ?: return emptyList()
        return SessionBuilder.dueLeftovers(words, progress, currentLesson, todayEpochDay)
            .map { word -> item(word, progress[word.id]) }
    }

    val todayTotal: Int get() = count(WordStatus.NEW) + count(WordStatus.DUE) + leftovers().size

    /** Words in the next round: one quarter of the current lesson (see SessionBuilder). */
    val plannedSessionSize: Int
        get() = lesson?.let { number ->
            SessionBuilder.nextSession(words, progress, number, todayEpochDay).size
        } ?: 0

    // ------------------------------------------------------ boxes and chart

    /** Words per Leitner box 1..8 (only words that have been read at least once). */
    fun boxCounts(): List<Int> =
        (Leitner.FIRST_BOX..Leitner.MAX_BOX).map { box ->
            progress.values.count { !it.isNew && it.box == box }
        }

    /** Words never read, across the whole book. */
    fun unseenCount(): Int = words.count { progress[it.id]?.isNew != false }

    /** Unread words in lessons before the current one (normally 0: lessons open in order). */
    fun unreadBefore(): Int {
        val current = lesson ?: return 0
        return words.count { it.lesson < current && progress[it.id]?.isNew != false }
    }

    /** One bar per lesson: share of words read, and share that reached box 4 or higher. */
    fun lessonBars(): List<LessonBar> = LessonPlan.lessonNumbers(words).map { number ->
        val inLesson = words.filter { it.lesson == number }
        val total = inLesson.size.coerceAtLeast(1)
        LessonBar(
            lesson = number,
            read = inLesson.count { progress[it.id]?.isNew == false }.toFloat() / total,
            strong = inLesson.count { (progress[it.id]?.box ?: 0) >= 4 }.toFloat() / total
        )
    }

    val totalWords: Int get() = words.size

    // --------------------------------------------------------------- session

    var sessionQueue by mutableStateOf<List<Word>>(emptyList())
        private set

    var sessionIndex by mutableIntStateOf(0)
        private set

    var sessionActive by mutableStateOf(false)
        private set

    var revealed by mutableStateOf(false)
        private set

    var sessionTotal by mutableIntStateOf(0)
        private set

    var answeredCorrect by mutableIntStateOf(0)
        private set

    var answeredWrong by mutableIntStateOf(0)
        private set

    val currentWord: Word? get() = sessionQueue.getOrNull(sessionIndex)

    /** True once the last card has been answered. */
    val sessionFinished: Boolean get() = sessionActive && currentWord == null

    fun startSession() {
        val currentLesson = lesson ?: return
        val queue = SessionBuilder.nextSession(
            words = words,
            progress = progress,
            lesson = currentLesson,
            todayEpochDay = todayEpochDay
        )
        if (queue.isEmpty()) return

        sessionQueue = queue
        sessionTotal = queue.size
        sessionIndex = 0
        revealed = false
        answeredCorrect = 0
        answeredWrong = 0
        sessionActive = true
    }

    fun reveal() {
        revealed = true
    }

    fun answer(known: Boolean) {
        val word = currentWord ?: return
        val state = progress[word.id]

        val updated = if (known) {
            Leitner.onKnown(word.id, state, todayEpochDay)
        } else {
            Leitner.onUnknown(word.id, state, todayEpochDay)
        }

        progress = progress + (word.id to updated)
        store.save(progress)

        if (known) answeredCorrect++ else answeredWrong++

        // 🔴 words get one more turn before the session ends.
        if (!known) sessionQueue = sessionQueue + word

        sessionIndex++
        revealed = false

        if (currentWord == null) sessionActive = false
    }

    fun exitSession() {
        sessionActive = false
        sessionQueue = emptyList()
        sessionIndex = 0
        revealed = false
    }

    /** The nearest upcoming review day, for the empty state. */
    fun nextReviewDate(): String? = items()
        .filter { it.status == WordStatus.LATER }
        .mapNotNull { it.nextReviewDate }
        .minByOrNull { it.epochDay }
        ?.let { JalaliCalendar.formatShort(it) }
}

class TodayViewModelFactory(
    private val words: List<Word>,
    private val store: ProgressStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return TodayViewModel(words = words, store = store) as T
    }
}
