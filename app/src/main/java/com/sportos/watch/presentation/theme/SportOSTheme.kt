package com.sportos.watch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

// Core Brand Palette
val CalopeiaPureBlack = Color(0xFF000000)
val CalopeiaCardDark = Color(0xFF141416)
val CalopeiaCardBorder = Color(0xFF26262B)
val CalopeiaCrimson = Color(0xFFE53935)
val CalopeiaCrimsonBright = Color(0xFFFF1744)
val CalopeiaCrimsonDim = Color(0xFFB71C1C)
val CalopeiaAmber = Color(0xFFFF9100)
val CalopeiaNeonGreen = Color(0xFF00E676)
val CalopeiaCyan = Color(0xFF00E5FF)
val CalopeiaTextWhite = Color(0xFFFFFFFF)
val CalopeiaTextMuted = Color(0xFFA0A0A8)

// Physiological Heart Rate Zones
object HrZones {
    val Zone1 = Color(0xFF90A4AE) // Recovery / Warm-up
    val Zone2 = Color(0xFF00E5FF) // Aerobic / Fat Burn
    val Zone3 = Color(0xFF00E676) // Tempo / Aerobic Endurance
    val Zone4 = Color(0xFFFF9100) // Threshold / Hardcore
    val Zone5 = Color(0xFFFF1744) // Anaerobic / Redline Max

    fun getZone(bpm: Double): Int {
        return when {
            bpm < 120 -> 1
            bpm < 140 -> 2
            bpm < 160 -> 3
            bpm < 175 -> 4
            else -> 5
        }
    }

    fun getZoneColor(bpm: Double): Color {
        return when (getZone(bpm)) {
            1 -> Zone1
            2 -> Zone2
            3 -> Zone3
            4 -> Zone4
            else -> Zone5
        }
    }

    fun getZoneLabel(bpm: Double): String {
        return when (getZone(bpm)) {
            1 -> "Zone 1 • Recovery"
            2 -> "Zone 2 • Aerobic"
            3 -> "Zone 3 • Tempo"
            4 -> "Zone 4 • Threshold"
            else -> "Zone 5 • Redline"
        }
    }
}

private val colorScheme = ColorScheme(
    primary = CalopeiaCrimson,
    primaryDim = CalopeiaCrimsonDim,
    secondary = CalopeiaCrimsonBright,
    secondaryDim = CalopeiaCrimsonDim,
    tertiary = CalopeiaAmber,
    tertiaryDim = Color(0xFFFF6D00),
    background = CalopeiaPureBlack,
    onPrimary = CalopeiaTextWhite,
    onSecondary = CalopeiaTextWhite,
    onTertiary = CalopeiaTextWhite,
    onBackground = CalopeiaTextWhite,
    surface = CalopeiaCardDark,
    onSurface = CalopeiaTextWhite,
    onSurfaceVariant = CalopeiaTextMuted,
    outline = CalopeiaCardBorder,
    error = Color(0xFFFF5252),
    onError = CalopeiaPureBlack
)

@Composable
fun SportOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

