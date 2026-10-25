package com.example.solmaz.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solmaz.R

// ---------------------------------------------------------------- palette
// White paper + pink accents, the way macOS Tahoe's Liquid Glass surfaces look:
// near-white fills, soft pink glows, deep plum text for contrast.
val Pink50 = Color(0xFFFFF4F8)
val Pink100 = Color(0xFFFFE6F0)
val Pink200 = Color(0xFFFFCBE0)
val Pink300 = Color(0xFFFFA8C8)
val Pink400 = Color(0xFFFF82AD)
val Pink500 = Color(0xFFFF5C8F) // primary accent
val Pink600 = Color(0xFFE74377)

val Ink = Color(0xFF2A2230)
val InkSoft = Color(0xFF6F5F69)
val Mint = Color(0xFF3FCFAE)
val SurfaceWhite = Color(0xFFFFFBFD)

// ---------------------------------------------------------------- typography
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int) = TextStyle(
    fontFamily = Vazirmatn,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    color = Ink
)

val AppTypography = Typography(
    displayLarge = style(52, FontWeight.Black, 60),
    displayMedium = style(42, FontWeight.Black, 50),
    headlineLarge = style(28, FontWeight.Bold, 36),
    headlineMedium = style(24, FontWeight.Bold, 32),
    headlineSmall = style(20, FontWeight.Bold, 28),
    titleLarge = style(18, FontWeight.Bold, 26),
    titleMedium = style(16, FontWeight.Medium, 24),
    titleSmall = style(14, FontWeight.Medium, 22),
    bodyLarge = style(16, FontWeight.Normal, 26),
    bodyMedium = style(14, FontWeight.Normal, 24),
    bodySmall = style(12, FontWeight.Normal, 20),
    labelLarge = style(14, FontWeight.Bold, 20),
    labelMedium = style(12, FontWeight.Medium, 18),
    labelSmall = style(11, FontWeight.Medium, 16)
)

// ---------------------------------------------------------------- shapes
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val AppColors = lightColorScheme(
    primary = Pink500,
    onPrimary = Color.White,
    primaryContainer = Pink100,
    onPrimaryContainer = Pink600,
    secondary = Mint,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF8F1),
    onSecondaryContainer = Color(0xFF0B5B4B),
    background = SurfaceWhite,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Pink50,
    onSurfaceVariant = InkSoft,
    outline = Pink200,
    outlineVariant = Pink100
)

@Composable
fun SolmazTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
