package com.example.solmaz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.JalaliDate
import com.example.solmaz.model.fa
import com.example.solmaz.ui.components.Chip
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Mint
import com.example.solmaz.ui.theme.Pink100
import com.example.solmaz.ui.theme.Pink300
import com.example.solmaz.ui.theme.Pink500
import java.time.LocalDate

enum class SolmazDestination(val title: String) {
    HOME("سولماز"),
    STUDY("مرور لایتنر"),
    CALENDAR("تقویم شمسی"),
    WORDS("واژگان"),
    PROGRESS("آمار پیشرفت"),
    SETTINGS("تنظیمات")
}

@Composable
fun SolmazNavigation(
    vm: TodayViewModel,
    userName: String?,
    loadError: String?,
    onSpeak: (String, SpeechLang) -> Unit,
    speechRate: Float,
    onSpeechRate: (Float) -> Unit
) {
    var destination by remember { mutableStateOf(SolmazDestination.HOME) }

    BackHandler(enabled = destination != SolmazDestination.HOME || vm.sessionActive) {
        if (vm.sessionActive) {
            vm.exitSession()
            destination = SolmazDestination.STUDY
        } else {
            destination = SolmazDestination.HOME
        }
    }

    if (vm.sessionActive) {
        ReviewSessionScreen(
            vm = vm,
            onExit = { vm.exitSession(); destination = SolmazDestination.STUDY },
            onSpeak = onSpeak,
            userName = userName
        )
        return
    }

    when (destination) {
        SolmazDestination.HOME -> HomeDashboard(
            vm = vm,
            userName = userName,
            onOpen = { destination = it }
        )
        SolmazDestination.STUDY -> {
            DestinationFrame(title = SolmazDestination.STUDY.title, onBack = { destination = SolmazDestination.HOME }) {
                TodayScreen(
                    vm = vm,
                    onStart = { vm.startSession() },
                    userName = userName,
                    loadError = loadError,
                    onSpeak = onSpeak,
                    speechRate = speechRate,
                    onSpeechRate = onSpeechRate
                )
            }
        }
        SolmazDestination.CALENDAR -> DestinationFrame(title = SolmazDestination.CALENDAR.title, onBack = { destination = SolmazDestination.HOME }) {
            JalaliCalendarScreen(vm)
        }
        SolmazDestination.WORDS -> DestinationFrame(title = SolmazDestination.WORDS.title, onBack = { destination = SolmazDestination.HOME }) {
            VocabularyScreen(vm, onSpeak)
        }
        SolmazDestination.PROGRESS -> DestinationFrame(title = SolmazDestination.PROGRESS.title, onBack = { destination = SolmazDestination.HOME }) {
            ProgressOverviewScreen(vm)
        }
        SolmazDestination.SETTINGS -> DestinationFrame(title = SolmazDestination.SETTINGS.title, onBack = { destination = SolmazDestination.HOME }) {
            SettingsScreen(rate = speechRate, onRate = onSpeechRate)
        }
    }
}

@Composable
private fun DestinationFrame(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "بازگشت", tint = Pink500)
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink)
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun HomeDashboard(
    vm: TodayViewModel,
    userName: String?,
    onOpen: (SolmazDestination) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (userName.isNullOrBlank()) "سلام، خوش اومدی" else "سلام $userName",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(JalaliCalendar.formatLong(vm.today), color = InkSoft, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
                Text("برنامهٔ امروز", color = InkSoft, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "${vm.todayTotal.fa} لغت برای یادگیری و مرور",
                    color = Pink500,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth().clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF82AD), Pink500)))
                        .clickable { onOpen(SolmazDestination.STUDY) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("رفتن به مرور امروز", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Text("بخش‌های برنامه", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold) }
        item {
            MenuTile(
                title = "تقویم شمسی",
                subtitle = "روزهای مرور و برنامهٔ آینده",
                icon = Icons.Rounded.CalendarMonth,
                accent = Pink500,
                onClick = { onOpen(SolmazDestination.CALENDAR) }
            )
        }
        item {
            MenuTile(
                title = "مرور لایتنر",
                subtitle = "${vm.todayTotal.fa} لغت در برنامهٔ امروز",
                icon = Icons.Rounded.MenuBook,
                accent = Color(0xFFB84F83),
                onClick = { onOpen(SolmazDestination.STUDY) }
            )
        }
        item {
            MenuTile(
                title = "واژگان",
                subtitle = "جست‌وجو در کل بانک لغات",
                icon = Icons.Rounded.Translate,
                accent = Color(0xFF238A78),
                onClick = { onOpen(SolmazDestination.WORDS) }
            )
        }
        item {
            MenuTile(
                title = "آمار پیشرفت",
                subtitle = "لغات خوانده‌شده و وضعیت جعبه‌ها",
                icon = Icons.Rounded.BarChart,
                accent = Color(0xFF6E70C9),
                onClick = { onOpen(SolmazDestination.PROGRESS) }
            )
        }
        item {
            MenuTile(
                title = "تنظیمات",
                subtitle = "تنظیم سرعت تلفظ",
                icon = Icons.Rounded.Settings,
                accent = InkSoft,
                onClick = { onOpen(SolmazDestination.SETTINGS) }
            )
        }
        item {
            Text(
                text = "سولماز • یادگیری روزانه، قدم‌به‌قدم",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                color = InkSoft,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun MenuTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        corner = 22.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 17.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = InkSoft, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "باز کردن $title", tint = InkSoft)
        }
    }
}

@Composable
private fun JalaliCalendarScreen(vm: TodayViewModel) {
    var shownMonth by remember { mutableIntStateOf(vm.today.month) }
    var shownYear by remember { mutableIntStateOf(vm.today.year) }
    val firstWeekday = JalaliCalendar.firstDayOfWeek(shownYear, shownMonth)
    val days = JalaliCalendar.monthLength(shownYear, shownMonth)
    val counts = vm.calendarReviewCounts(shownYear, shownMonth)
    val cells = List(firstWeekday) { 0 } + (1..days).toList()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(CircleShape).clickable {
                        if (shownMonth == 1) { shownMonth = 12; shownYear-- } else shownMonth--
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "ماه قبل", tint = Pink500)
                    }
                    Text(
                        JalaliCalendar.formatMonth(shownYear, shownMonth),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Box(Modifier.size(38.dp).clip(CircleShape).clickable {
                        if (shownMonth == 12) { shownMonth = 1; shownYear++ } else shownMonth++
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد", tint = Pink500)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    JalaliCalendar.WEEK_DAYS.forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = InkSoft, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                cells.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (week + List(7 - week.size) { 0 }).forEach { day ->
                            Box(
                                Modifier.weight(1f).height(48.dp).padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            day == vm.today.day && shownMonth == vm.today.month && shownYear == vm.today.year -> Pink100
                                            day > 0 && (counts[day] ?: 0) > 0 -> Color(0xFFDDF8F1)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day > 0) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(day.fa, color = if (day == vm.today.day && shownMonth == vm.today.month && shownYear == vm.today.year) Pink500 else Ink, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        if ((counts[day] ?: 0) > 0) {
                                            Box(Modifier.padding(top = 2.dp).size(5.dp).clip(CircleShape).background(Mint))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(Mint))
                    Spacer(Modifier.width(8.dp))
                    Text("نقطهٔ سبز یعنی برای آن روز مرور زمان‌بندی شده است.", color = InkSoft, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(6.dp))
                Text("تعداد مرورهای زمان‌بندی‌شده در این ماه: ${counts.values.sum().fa}", color = Ink, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun VocabularyScreen(vm: TodayViewModel, onSpeak: (String, SpeechLang) -> Unit) {
    var query by remember { mutableStateOf("") }
    val words = vm.allItems().filter {
        query.isBlank() || it.word.term.contains(query, ignoreCase = true) || it.word.meaning.contains(query, ignoreCase = true)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Search, contentDescription = "جست‌وجو", tint = Pink500)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("جست‌وجوی واژه یا معنی…", color = InkSoft)
                        inner()
                    }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("${words.size.fa} واژه", color = InkSoft, style = MaterialTheme.typography.labelMedium)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(words, key = { it.word.id }) { item ->
                GlassCard(Modifier.fillMaxWidth(), corner = 18.dp, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.word.term, color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text(item.word.meaning, color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(5.dp))
                            Chip(
                                text = when (item.status) {
                                    WordStatus.NEW -> "نخوانده"
                                    WordStatus.DUE -> "نوبت مرور"
                                    WordStatus.LATER -> item.nextReviewDate?.let { JalaliCalendar.formatShort(it) } ?: "بعداً"
                                }
                            )
                        }
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Pink100).clickable {
                            onSpeak(item.word.term, SpeechLang.GERMAN)
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = "تلفظ ${item.word.term}", tint = Pink500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressOverviewScreen(vm: TodayViewModel) {
    val learned = vm.totalWords - vm.unseenCount()
    val boxCounts = vm.boxCounts()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("نمای کلی", color = InkSoft, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text("${learned.fa} از ${vm.totalWords.fa} واژه شروع شده", color = Pink500, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Pink100)) {
                    Box(Modifier.fillMaxWidth(if (vm.totalWords == 0) 0f else learned.toFloat() / vm.totalWords).height(10.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Pink300, Pink500))))
                }
                Spacer(Modifier.height(8.dp))
                Text("${vm.unseenCount().fa} واژه هنوز نخوانده‌اند", color = InkSoft, style = MaterialTheme.typography.bodySmall)
            }
        }
        item { Text("وضعیت جعبه‌های لایتنر", color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(boxCounts.size) { index ->
            GlassCard(Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("جعبهٔ ${(index + 1).fa}", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("${boxCounts[index].fa} واژه", color = Pink500, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("هر ${com.example.solmaz.study.Leitner.intervalDays(index + 1).fa} روز", color = InkSoft, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(rate: Float, onRate: (Float) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Text("سرعت تلفظ", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("سرعت دلخواهت را انتخاب کن؛ این تنظیم روی تلفظ واژه‌ها اثر می‌گذارد.", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            Text(
                text = when {
                    rate < 0.6f -> "آرام"
                    rate < 0.8f -> "متوسط"
                    else -> "عادی"
                },
                color = Pink500,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = rate,
                onValueChange = onRate,
                valueRange = 0.4f..1.0f
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("آرام" to 0.5f, "متوسط" to 0.7f, "عادی" to 0.9f).forEach { (label, value) ->
                    val selected = kotlin.math.abs(rate - value) < 0.06f
                    Chip(
                        text = label,
                        modifier = Modifier.clickable { onRate(value) },
                        container = if (selected) Pink500 else Pink100,
                        contentColor = if (selected) Color.White else Pink500
                    )
                }
            }
        }
    }
}
