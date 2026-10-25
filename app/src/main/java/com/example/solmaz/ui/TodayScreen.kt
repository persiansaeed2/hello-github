package com.example.solmaz.ui

import androidx.compose.foundation.background
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
    loadError: String? = null,
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items = vm.items()
    val leftovers = vm.leftovers()

    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Header(vm) }
            item { TodayCard(vm) }
            item { Legend() }

            if (items.isEmpty() && leftovers.isEmpty()) {
                item { EmptyWords(loadError) }
            } else {
                vm.lesson?.let { lesson ->
                    item { SectionTitle("درس ${lesson.fa}") }
                }
                items(items, key = { it.word.id }) { item ->
                    WordRow(item = item, onSpeak = onSpeak)
                }

                if (leftovers.isNotEmpty()) {
                    item { SectionTitle("مانده از قبل — اگر وقت و حوصله داری", spaced = true) }
                    items(leftovers, key = { "left-${it.word.id}" }) { item ->
                        WordRow(item = item, onSpeak = onSpeak)
                    }
                }
            }
        }

        if (vm.todayTotal > 0) {
            StartBar(
                words = vm.todayTotal,
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
private fun Header(vm: TodayViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "امروز", style = MaterialTheme.typography.headlineLarge, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                text = JalaliCalendar.formatLong(vm.today),
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
        }
        IconBadge(icon = Icons.Rounded.MenuBook, badgeSize = 48.dp)
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
            vm.lesson?.let { Chip(text = "درس ${it.fa}", container = Color.White) }
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
