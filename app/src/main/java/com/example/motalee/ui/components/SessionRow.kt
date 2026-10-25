package com.example.motalee.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motalee.model.StudySession
import com.example.motalee.model.fa
import com.example.motalee.ui.theme.Ink
import com.example.motalee.ui.theme.InkSoft
import com.example.motalee.ui.theme.Pink300

/** One study session inside a glass card, shared by the today and calendar screens. */
@Composable
fun SessionRow(
    session: StudySession,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        corner = 22.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Rounded.MenuBook, badgeSize = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${session.minutes.fa} دقیقه",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "حذف",
                    tint = Pink300
                )
            }
        }
    }
}
