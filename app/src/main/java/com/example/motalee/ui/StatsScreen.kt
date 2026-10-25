package com.example.motalee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motalee.model.DayStat
import com.example.motalee.model.JalaliCalendar
import com.example.motalee.model.fa
import com.example.motalee.model.formatMinutes
import com.example.motalee.ui.components.Chip
import com.example.motalee.ui.components.GlassCard
import com.example.motalee.ui.components.IconBadge
import com.example.motalee.ui.components.SectionHeader
import com.example.motalee.ui.theme.Ink
import com.example.motalee.ui.theme.InkSoft
import com.example.motalee.ui.theme.Pink100
import com.example.motalee.ui.theme.Pink300
import com.example.motalee.ui.theme.Pink500

@Composable
fun StatsScreen(vm: StudyViewModel, modifier: Modifier = Modifier) {
    val subjects = vm.subjectStats()
    val best = vm.bestDay()
    val total = vm.totalMinutes

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "آمار مطالعه",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "تصویر کلی از تلاش‌هایت",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                }
                IconBadge(icon = Icons.Rounded.Insights, badgeSize = 48.dp)
            }
        }

        item {
            TotalsCard(
                totalMinutes = total,
                days = vm.totalDays,
                streak = vm.streakDays()
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "به‌تفکیک درس", modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
                if (subjects.isEmpty()) {
                    Text(
                        text = "هنوز درسی ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                } else {
                    subjects.forEachIndexed { index, stat ->
                        SubjectBar(
                            name = stat.subject,
                            minutes = stat.minutes,
                            fraction = stat.minutes.toFloat() / total.coerceAtLeast(1)
                        )
                        if (index != subjects.lastIndex) Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }

        if (best != null) {
            item { BestDayCard(best = best) }
        }
    }
}

@Composable
private fun TotalsCard(totalMinutes: Int, days: Int, streak: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(22.dp)) {
        Text(
            text = "مجموع مطالعه",
            style = MaterialTheme.typography.titleMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatMinutes(totalMinutes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Pink500
        )
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MiniStat(label = "روزهای فعال", value = days.fa, modifier = Modifier.weight(1f))
            MiniStat(label = "روز پیاپی", value = streak.fa, modifier = Modifier.weight(1f))
            MiniStat(
                label = "میانگین روزانه",
                value = (if (days == 0) 0 else totalMinutes / days).fa,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SubjectBar(name: String, minutes: Int, fraction: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${minutes.fa} دقیقه",
                style = MaterialTheme.typography.labelMedium,
                color = InkSoft
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Pink100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Pink300, Pink500)))
            )
        }
    }
}

@Composable
private fun BestDayCard(best: DayStat) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Rounded.Star, badgeSize = 42.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "بهترین روز",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSoft
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = JalaliCalendar.formatShort(best.date),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink
                )
            }
            Chip(text = formatMinutes(best.minutes), container = Pink100)
        }
    }
}

