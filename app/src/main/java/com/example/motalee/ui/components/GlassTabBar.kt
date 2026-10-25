package com.example.motalee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.motalee.ui.theme.InkSoft
import com.example.motalee.ui.theme.Pink400
import com.example.motalee.ui.theme.Pink500

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("امروز", Icons.Rounded.Today),
    TabItem("تقویم", Icons.Rounded.CalendarMonth),
    TabItem("آمار", Icons.Rounded.Insights)
)

/** Floating translucent tab bar, the Liquid-Glass style bottom bar of the app. */
@Composable
fun GlassTabBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 18.dp, shape = CircleShape, ambientColor = Pink400.copy(alpha = 0.4f), spotColor = Pink400.copy(alpha = 0.45f))
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.80f))
            .border(1.dp, Color.White.copy(alpha = 0.95f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TABS.forEachIndexed { index, tab ->
            val active = index == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            Brush.horizontalGradient(listOf(Pink400, Pink500))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (active) Color.White else InkSoft,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.White else InkSoft
                )
            }
        }
    }
}
