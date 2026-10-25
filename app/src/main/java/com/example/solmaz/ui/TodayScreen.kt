package com.example.solmaz.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.StudySession
import com.example.solmaz.model.fa
import com.example.solmaz.model.formatMinutes
import com.example.solmaz.ui.components.Chip
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.components.IconBadge
import com.example.solmaz.ui.components.ProgressRing
import com.example.solmaz.ui.components.SectionHeader
import com.example.solmaz.ui.components.SessionRow
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Pink100
import com.example.solmaz.ui.theme.Pink200
import com.example.solmaz.ui.theme.Pink300
import com.example.solmaz.ui.theme.Pink400
import com.example.solmaz.ui.theme.Pink500

@Composable
fun TodayScreen(
    vm: StudyViewModel,
    onAdd: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val todaySessions = vm.sessionsOn(vm.today.epochDay)
    val week = vm.lastSevenDays()
    val progress by animateFloatAsState(
        targetValue = (vm.todayMinutes.toFloat() / StudyViewModel.DAILY_GOAL_MINUTES)
            .coerceIn(0f, 1f),
        label = "goal-progress"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TodayHeader(vm) }

        item {
            GoalCard(
                minutes = vm.todayMinutes,
                progress = progress,
                streak = vm.streakDays()
            )
        }

        item { WeekCard(week = week, onAdd = onAdd) }

        item {
            SectionHeader(
                title = "جلسه‌های امروز",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                trailing = { Chip(text = "${todaySessions.size.fa} جلسه") }
            )
        }

        if (todaySessions.isEmpty()) {
            item { EmptyTodayCard(onAdd = { onAdd(vm.today.epochDay) }) }
        } else {
            items(todaySessions, key = { it.id }) { session ->
                SessionRow(session = session, onDelete = { onDelete(session.id) })
            }
        }
    }
}

@Composable
private fun TodayHeader(vm: StudyViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "سلام 👋",
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
        IconBadge(icon = Icons.Rounded.MenuBook, badgeSize = 48.dp)
    }
}

@Composable
private fun GoalCard(minutes: Int, progress: Float, streak: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(132.dp),
                strokeWidth = 14.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = minutes.fa,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "دقیقه",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "هدف امروز",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSoft
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${(progress * 100).toInt().fa}٪",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Pink500
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "از ${formatMinutes(StudyViewModel.DAILY_GOAL_MINUTES)} مطالعه",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(12.dp))
                Chip(
                    text = if (streak > 0) "🔥 ${streak.fa} روز پیاپی" else "امروز شروع کن"
                )
            }
        }
    }
}

@Composable
private fun WeekCard(week: List<DayStat>, onAdd: (Long) -> Unit) {
    val maxMinutes = maxOf(week.maxOfOrNull { it.minutes } ?: 0, 30)
    val todayEpochDay = week.lastOrNull()?.date?.epochDay

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "هفت روز گذشته",
            modifier = Modifier.fillMaxWidth(),
            trailing = {
                Text(
                    text = formatMinutes(week.sumOf { it.minutes }),
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSoft
                )
            }
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            week.forEach { day ->
                val fraction = day.minutes.toFloat() / maxMinutes
                val isToday = day.date.epochDay == todayEpochDay
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onAdd(day.date.epochDay) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (day.minutes == 0) "" else day.minutes.fa,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) Pink500 else InkSoft
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height((96 * fraction).coerceAtLeast(6f).dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) {
                                    Brush.verticalGradient(listOf(Pink400, Pink500))
                                } else {
                                    Brush.verticalGradient(listOf(Pink200, Pink100))
                                }
                            )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = JalaliCalendar.WEEK_DAYS[day.date.dayOfWeek],
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) Pink500 else InkSoft,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodayCard(onAdd: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(icon = Icons.Rounded.Timer, badgeSize = 52.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "هنوز امروز مطالعه‌ای ثبت نکردی",
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "با دکمهٔ پایین، اولین جلسهٔ امروز را اضافه کن.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            PinkPill(text = "ثبت مطالعهٔ امروز", onClick = onAdd)
        }
    }
}

/** Primary pill button used for the main call to action. */
@Composable
fun PinkPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(Pink400, Pink500))
                } else {
                    Brush.horizontalGradient(listOf(Pink200, Pink200))
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}



