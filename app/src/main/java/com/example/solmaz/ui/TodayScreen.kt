package com.example.solmaz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.solmaz.study.Leitner
import com.example.solmaz.ui.theme.Pink300
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.fa
import com.example.solmaz.ui.components.Chip
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.components.IconBadge
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Mint
import com.example.solmaz.ui.theme.Pink100
import com.example.solmaz.ui.theme.Pink200
import com.example.solmaz.ui.theme.Pink500

/** Colour of a status, shared by the list rows and the legend. */
fun statusColor(status: WordStatus): Color = when (status) {
    WordStatus.DUE -> Pink500
    WordStatus.NEW -> Mint
    WordStatus.LATER -> InkSoft
}

fun statusLabel(item: WordItem): String = when (item.status) {
    WordStatus.DUE -> "مرور ${item.reviewNumber.fa}"
    WordStatus.NEW -> "تازه"
    WordStatus.LATER -> item.nextReviewDate?.let { JalaliCalendar.formatShort(it) } ?: "بعداً"
}

/** The one and only screen: what to study today. */
@Composable
fun TodayScreen(
    vm: TodayViewModel,
    onStart: () -> Unit,
    userName: String? = null,
    loadError: String? = null,
    onSpeak: (String, SpeechLang) -> Unit = { _, _ -> },
    speechRate: Float = 0.7f,
    onSpeechRate: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items = vm.items()
    val leftovers = vm.leftovers()
    var showAll by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) {
        SettingsDialog(rate = speechRate, onRate = onSpeechRate, onClose = { showSettings = false })
    }

    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Header(vm, userName, onMenu = { showSettings = true }) }
            item { TodayCard(vm) }
            item { SectionTitle("جعبه‌های لایتنر") }
            item { LeitnerBoxes(vm) }
            item { SectionTitle("نمودار پیشرفت") }
            item { ProgressChart(vm) }
            item { PreviousLessonsCard(vm) }
            item { Legend() }

            if (items.isEmpty() && leftovers.isEmpty()) {
                item { EmptyWords(loadError) }
            } else {
                vm.lesson?.let { lesson ->
                    val title = vm.lessonTitle?.let { " — $it" }.orEmpty()
                    item { SectionTitle("درس ${lesson.fa}$title") }
                }
                items(if (showAll) items else items.take(PREVIEW), key = { it.word.id }) { item ->
                    WordRow(item = item, onSpeak = { term -> onSpeak(term, SpeechLang.GERMAN) })
                }
                if (items.size > PREVIEW) {
                    item { ShowMore(showAll, items.size) { showAll = !showAll } }
                }

                if (leftovers.isNotEmpty()) {
                    item { SectionTitle("مانده از قبل — اگر وقت و حوصله داری", spaced = true) }
                    items(leftovers.take(PREVIEW), key = { "left-${it.word.id}" }) { item ->
                        WordRow(item = item, onSpeak = { term -> onSpeak(term, SpeechLang.GERMAN) })
                    }
                }
            }
        }

        if (vm.todayTotal > 0) {
            StartBar(
                words = vm.plannedSessionSize,
                onStart = onStart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun Header(vm: TodayViewModel, userName: String?, onMenu: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (userName.isNullOrBlank()) "سلام 👋" else "سلام $userName 👋",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = JalaliCalendar.formatLong(vm.today),
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
        }
        Box(modifier = Modifier.clip(CircleShape).clickable(onClick = onMenu)) {
            IconBadge(icon = Icons.Rounded.Menu, badgeSize = 48.dp)
        }
    }
}

@Composable
private fun TodayCard(vm: TodayViewModel) {
    val due = vm.count(WordStatus.DUE)
    val fresh = vm.count(WordStatus.NEW)

    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Text(
            text = if (vm.todayTotal == 0) "امروز کاری نمانده" else "برای امروز",
            style = MaterialTheme.typography.titleMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (vm.todayTotal == 0) "—" else "${vm.todayTotal.fa} لغت",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Pink500
        )
        if (vm.todayTotal == 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = vm.nextReviewDate()?.let { "مرور بعدی: $it" } ?: "لغتی برای مرور نیست",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(text = "${due.fa} نوبت مرور", container = Pink100, contentColor = Pink500)
            Chip(text = "${fresh.fa} تازه", container = Color(0xFFDDF8F1), contentColor = Color(0xFF0B5B4B))
            vm.leftovers().size.takeIf { it > 0 }?.let {
                Chip(text = "${it.fa} مانده از قبل", container = Color.White, contentColor = Pink500)
            }
            vm.lesson?.let { lesson ->
                Chip(
                    text = buildString {
                        append("درس ${lesson.fa}")
                        vm.lessonTitle?.let { append(" · $it") }
                    },
                    container = Color.White
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, spaced: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = InkSoft,
        modifier = Modifier.padding(top = if (spaced) 10.dp else 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun Legend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(WordStatus.DUE, "نوبت مرور")
        LegendDot(WordStatus.NEW, "تازه")
        LegendDot(WordStatus.LATER, "بعداً")
    }
}

@Composable
private fun LegendDot(status: WordStatus, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor(status))
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}

@Composable
private fun WordRow(item: WordItem, onSpeak: (String) -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 20.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(statusColor(item.status))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.word.term,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.word.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.75f))
                    .clickable { onSpeak(item.word.term) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "تلفظ ${item.word.term}",
                    tint = Pink500,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Chip(
                text = statusLabel(item),
                container = when (item.status) {
                    WordStatus.DUE -> Pink100
                    WordStatus.NEW -> Color(0xFFDDF8F1)
                    WordStatus.LATER -> Pink200.copy(alpha = 0.35f)
                },
                contentColor = statusColor(item.status)
            )
        }
    }
}

@Composable
private fun EmptyWords(loadError: String?) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "فایل لغات خالی است",
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = loadError ?: "لغات را در assets/words.json بگذار تا اینجا بیایند.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StartBar(words: Int, onStart: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Color(0xFFFF82AD), Pink500)))
            .clickable(onClick = onStart)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "شروع مرور ${words.fa} لغت",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private const val PREVIEW = 5

@Composable
private fun ShowMore(expanded: Boolean, total: Int, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Pink100)
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (expanded) "نمایش کمتر" else "نمایش همهٔ ${total.fa} لغت",
            color = Pink500,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** The eight Leitner boxes: how many words sit in each, and its review interval in days. */
@Composable
private fun LeitnerBoxes(vm: TodayViewModel) {
    val counts = vm.boxCounts()
    val top = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            counts.forEachIndexed { index, count ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.fa, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((8 + 56 * count / top).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (count == 0) Pink100 else Pink400())
                    )
                    Spacer(Modifier.height(6.dp))
                    Text((index + 1).fa, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = FontWeight.Bold)
                    Text(Leitner.intervalDays(index + 1).fa, style = MaterialTheme.typography.labelSmall, color = InkSoft)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "شمارهٔ جعبه بالا، فاصلهٔ مرور (روز) پایین • ${vm.unseenCount().fa} لغت هنوز نخوانده",
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft
        )
    }
}

private fun Pink400() = Pink300

/** One bar per lesson (lesson 1 on the right): light = read, dark = learned (box 4 and up). */
@Composable
private fun ProgressChart(vm: TodayViewModel) {
    val bars = vm.lessonBars()
    val read = vm.totalWords - vm.unseenCount()
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            if (bars.isEmpty()) return@Canvas
            val gap = 4.dp.toPx()
            val barWidth = (size.width - gap * (bars.size - 1)) / bars.size
            val radius = CornerRadius(barWidth / 3f, barWidth / 3f)
            bars.forEachIndexed { i, bar ->
                val x = size.width - (i + 1) * barWidth - i * gap
                drawRoundRect(Pink100, Offset(x, 0f), Size(barWidth, size.height), radius)
                val readHeight = size.height * bar.read
                drawRoundRect(Pink300, Offset(x, size.height - readHeight), Size(barWidth, readHeight), radius)
                val strongHeight = size.height * bar.strong
                drawRoundRect(Pink500, Offset(x, size.height - strongHeight), Size(barWidth, strongHeight), radius)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${read.fa} از ${vm.totalWords.fa} لغت خوانده شده • هر ستون یک درس",
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft
        )
    }
}

/** Unread and overdue words of lessons before the current one. */
@Composable
private fun PreviousLessonsCard(vm: TodayViewModel) {
    val unread = vm.unreadBefore()
    val due = vm.leftovers().size
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Text("مانده از درس‌های قبل", style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (unread == 0 && due == 0) {
                "همهٔ لغات درس‌های قبل خوانده شده و مروری عقب نیفتاده ✓"
            } else {
                "${unread.fa} لغت نخوانده • ${due.fa} لغت نوبت مرور"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "درس بعدی وقتی باز می‌شود که همهٔ لغات درس قبل حداقل یک‌بار خوانده شود.",
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft
        )
    }
}

@Composable
private fun SettingsDialog(rate: Float, onRate: (Float) -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("بستن") } },
        title = { Text("تنظیمات") },
        text = {
            Column {
                Text("سرعت تلفظ", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("آرام" to 0.5f, "متوسط" to 0.7f, "عادی" to 0.9f).forEach { (label, value) ->
                        val on = kotlin.math.abs(rate - value) < 0.05f
                        Chip(
                            text = label,
                            modifier = Modifier.clickable { onRate(value) },
                            container = if (on) Pink500 else Pink100,
                            contentColor = if (on) Color.White else Pink500
                        )
                    }
                }
            }
        }
    )
}
