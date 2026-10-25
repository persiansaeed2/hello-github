package com.example.solmaz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.solmaz.model.JalaliDate
import com.example.solmaz.model.fa
import com.example.solmaz.model.formatMinutes
import com.example.solmaz.ui.components.Chip
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.components.SectionHeader
import com.example.solmaz.ui.components.SessionRow
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Pink100
import com.example.solmaz.ui.theme.Pink300
import com.example.solmaz.ui.theme.Pink400
import com.example.solmaz.ui.theme.Pink500

@Composable
fun CalendarScreen(
    vm: StudyViewModel,
    onAdd: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onShiftMonth: (Int) -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val year = vm.visibleYear
    val month = vm.visibleMonth
    val rows = monthRows(year, month)
    val studiedDays = vm.sessions.map { it.epochDay }.toSet()
    val selected = vm.selectedDate
    val selectedSessions = vm.sessionsOn(vm.selectedEpochDay)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MonthCard(
                year = year,
                month = month,
                rows = rows,
                studiedDays = studiedDays,
                selectedEpochDay = vm.selectedEpochDay,
                todayEpochDay = vm.today.epochDay,
                minutesInMonth = vm.minutesInMonth(year, month),
                studiedDaysInMonth = vm.studiedDaysInMonth(year, month),
                onShiftMonth = onShiftMonth,
                onToday = onToday,
                onSelect = { vm.selectDay(it) }
            )
        }

        item {
            SectionHeader(
                title = if (selected.epochDay == vm.today.epochDay) "امروز" else JalaliCalendar.formatShort(selected),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                trailing = {
                    Chip(
                        text = formatMinutes(selectedSessions.sumOf { it.minutes }),
                        container = Pink100
                    )
                }
            )
        }

        if (selectedSessions.isEmpty()) {
            item { EmptyDayCard(onAdd = { onAdd(selected.epochDay) }) }
        } else {
            items(selectedSessions, key = { it.id }) { session ->
                SessionRow(session = session, onDelete = { onDelete(session.id) })
            }
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PinkPill(text = "ثبت مطالعه در این روز", onClick = { onAdd(selected.epochDay) })
                }
            }
        }
    }
}

/** Builds a 7-column grid (Saturday first) padded with nulls for the leading/trailing gaps. */
private fun monthRows(year: Int, month: Int): List<List<JalaliDate?>> {
    val leading = JalaliCalendar.firstDayOfWeek(year, month)
    val days = JalaliCalendar.monthLength(year, month)
    val cells = ArrayList<JalaliDate?>(42)
    repeat(leading) { cells.add(null) }
    for (day in 1..days) cells.add(JalaliDate(year, month, day))
    while (cells.size % 7 != 0) cells.add(null)
    return cells.chunked(7)
}

@Composable
private fun MonthCard(
    year: Int,
    month: Int,
    rows: List<List<JalaliDate?>>,
    studiedDays: Set<Long>,
    selectedEpochDay: Long,
    todayEpochDay: Long,
    minutesInMonth: Int,
    studiedDaysInMonth: Int,
    onShiftMonth: (Int) -> Unit,
    onToday: () -> Unit,
    onSelect: (Long) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        // macOS-style segmented header: ‹ month › with a "today" shortcut.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onShiftMonth(-1) }) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "ماه قبل", tint = Pink500)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = JalaliCalendar.formatMonth(year, month),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatMinutes(minutesInMonth)} • ${studiedDaysInMonth.fa} روز",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSoft
                )
            }
            IconButton(onClick = { onShiftMonth(1) }) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد", tint = Pink500)
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            JalaliCalendar.WEEK_DAYS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSoft
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        isSelected = date != null && date.epochDay == selectedEpochDay,
                        isToday = date != null && date.epochDay == todayEpochDay,
                        hasSessions = date != null && date.epochDay in studiedDays,
                        onClick = { date?.let { onSelect(it.epochDay) } }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Chip(
                text = "برو به امروز",
                modifier = Modifier.clickable(onClick = onToday),
                container = Pink100,
                contentColor = Pink500
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.DayCell(
    date: JalaliDate?,
    isSelected: Boolean,
    isToday: Boolean,
    hasSessions: Boolean,
    onClick: () -> Unit
) {
    val base = Modifier
        .weight(1f)
        .aspectRatio(1f)
        .padding(3.dp)

    if (date == null) {
        Box(modifier = base)
        return
    }

    Box(
        modifier = base
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    Brush.linearGradient(listOf(Pink400, Pink500))
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .then(
                if (isToday && !isSelected) Modifier.border(1.5.dp, Pink400, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.day.fa,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> Pink500
                    hasSessions -> Ink
                    else -> InkSoft
                }
            )
            if (hasSessions) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Pink300)
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCard(onAdd: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "در این روز چیزی ثبت نشده",
                style = MaterialTheme.typography.titleSmall,
                color = Ink
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "با لمس روزهای تقویم، مطالعهٔ همان روز را ببین یا اضافه کن.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            PinkPill(text = "افزودن جلسه", onClick = onAdd)
        }
    }
}

