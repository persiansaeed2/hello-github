package com.example.solmaz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.model.JalaliDate
import com.example.solmaz.model.fa
import com.example.solmaz.ui.components.GlassCard
import com.example.solmaz.ui.theme.Ink
import com.example.solmaz.ui.theme.InkSoft
import com.example.solmaz.ui.theme.Pink100
import com.example.solmaz.ui.theme.Pink500

private val PRESETS = listOf(15, 30, 45, 60, 90, 120)

/**
 * macOS-style sheet for logging a study session: subject + duration, with quick
 * duration presets so a session is two taps away.
 */
@Composable
fun AddSessionDialog(
    date: JalaliDate,
    onDismiss: () -> Unit,
    onSave: (subject: String, minutes: Int) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var minutesText by remember { mutableStateOf("") }

    val minutes = minutesText.toIntOrNull() ?: 0
    val valid = minutes > 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.padding(22.dp)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 30.dp,
                contentPadding = PaddingValues(22.dp)
            ) {
                Text(
                    text = "ثبت مطالعه",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = JalaliCalendar.formatLong(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it.take(30) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("درس / موضوع") },
                    placeholder = { Text("مثلاً ریاضی") },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { input -> minutesText = input.filter { it.isDigit() }.take(4) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("مدت (دقیقه)") },
                    placeholder = { Text("۳۰") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(14.dp))
                DurationPresets(
                    minutesText = minutesText,
                    onSelect = { minutesText = it.toString() }
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "انصراف",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkSoft
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    PinkPill(
                        text = "ذخیره",
                        showIcon = false,
                        enabled = valid,
                        onClick = { onSave(subject, minutes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationPresets(minutesText: String, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PRESETS.forEach { preset ->
            val selected = minutesText == preset.toString()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected) Pink500 else Pink100)
                    .clickable { onSelect(preset) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.fa,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.White else Pink500
                )
            }
        }
    }
}
