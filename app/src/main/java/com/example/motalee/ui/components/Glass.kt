package com.example.motalee.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.motalee.ui.theme.Ink
import com.example.motalee.ui.theme.Pink100
import com.example.motalee.ui.theme.Pink200
import com.example.motalee.ui.theme.Pink300
import com.example.motalee.ui.theme.Pink400
import com.example.motalee.ui.theme.Pink50
import com.example.motalee.ui.theme.Pink500

/**
 * Soft white → pink canvas with out-of-focus pink glows: the backdrop that all
 * glass surfaces are layered on top of.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Pink50, Color(0xFFFFEAF2))
                )
            )
    ) {
        Glow(color = Pink300, glowSize = 340.dp, offsetX = 190.dp, offsetY = (-120).dp)
        Glow(color = Pink200, glowSize = 300.dp, offsetX = (-140).dp, offsetY = 250.dp)
        Glow(color = Color(0xFFFFD3E4), glowSize = 240.dp, offsetX = 150.dp, offsetY = 600.dp)
    }
}

@Composable
private fun BoxScope.Glow(
    color: Color,
    glowSize: Dp,
    offsetX: Dp,
    offsetY: Dp
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            // offset (not padding) because the glows intentionally hang off the screen edge.
            .offset(x = offsetX, y = offsetY)
            .size(glowSize)
            .background(
                Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent)),
                CircleShape
            )
    )
}

/**
 * The core macOS-like surface: translucent white, hairline highlight border and
 * a wide, very soft pink shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 28.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = Pink400.copy(alpha = 0.30f),
                spotColor = Pink400.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.94f), Color.White.copy(alpha = 0.66f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White, Pink200.copy(alpha = 0.70f))
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

/** Rounded square with a pink tint — the macOS "app icon" accent. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 42.dp,
    contentDescription: String? = null
) {
    val shape = RoundedCornerShape(badgeSize / 2.6f)
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Pink100, Color.White)))
            .border(1.dp, Color.White.copy(alpha = 0.9f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Pink500,
            modifier = Modifier.size(badgeSize * 0.52f)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        trailing?.invoke()
    }
}

/** Small rounded label, e.g. "امروز" or "−۱". */
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = Pink100,
    contentColor: Color = Pink500
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Circular goal indicator with a pink gradient stroke. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 13.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Pink100,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val sweep = 360f * progress.coerceIn(0f, 1f)
            if (sweep > 1f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(Pink300, Pink500, Pink400, Pink300)),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

