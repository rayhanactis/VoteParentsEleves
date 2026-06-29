package com.rayhanactis.voteparentseleves.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tailles volontairement grandes : public visé inclut des parents
// qui lisent peu / mal.
val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Black, lineHeight = 56.sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    labelLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    labelMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
)
