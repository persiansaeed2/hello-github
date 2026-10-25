package com.example.solmaz.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solmaz.model.fa
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Mint
import com.example.solmaz.ui.theme.Pink400
import com.example.solmaz.ui.theme.Pink500

private val Red = Color(0xFFFF4D6D)
private val Green = Color(0xFF2FBF8F)

/**
 * One word at a time: the term first, then «نمایش معنی», then 🟢 / 🔴.
 * A red answer puts the word back at the end of the same session.
 */
@Composable
fun ReviewSessionScreen(
    vm: TodayViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val word = vm.currentWord

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (word == null) "پایان" else "${(vm.sessionIndex + 1).fa} از ${vm.sessionQueue.size.fa}",
                style = MaterialTheme.typography.titleSmall,
                color = InkSoft
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .clickable(onClick = onExit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "بستن",
                    tint = InkSoft,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SessionProgress(current = vm.sessionIndex + 1, total = vm.sessionQueue.size)
        Spacer(Modifier.height(24.dp))

        if (word == null) {
            SessionSummary(vm)
        } else {
            WordCard(word.term, word.meaning, vm.revealed, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(18.dp))
            if (vm.revealed) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnswerButton(
                        text = "بلد نبودم",
                        icon = Icons.Rounded.Close,
                        color = Red,
                        onClick = { vm.answer(known = false) },
                        modifier = Modifier.weight(1f)
                    )
                    AnswerButton(
                        text = "بلدم",
                        icon = Icons.Rounded.Check,
                        color = Green,
                        onClick = { vm.answer(known = true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                RevealButton(onClick = { vm.reveal() })
            }
        }
    }
}

@Composable
private fun WordCard(
    term: String,
    meaning: String,
    revealed: Boolean,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        corner = 30.dp,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = term,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = Ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            if (revealed) {
                Text(
                    text = meaning,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = Pink500,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "معنی را به یاد آوردی؟",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
        }
    }
}

@Composable
private fun RevealButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Pink400, Pink500)))
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "نمایش معنی",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnswerButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SessionProgress(current: Int, total: Int) {
    val fraction = (current.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Mint, Pink400)))
        )
    }
}

@Composable
private fun SessionSummary(vm: TodayViewModel) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("این دور تمام شد", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SummaryStat("درست", vm.answeredCorrect, Green)
                SummaryStat("اشتباه", vm.answeredWrong, Red)
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.fa,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
    }
}
