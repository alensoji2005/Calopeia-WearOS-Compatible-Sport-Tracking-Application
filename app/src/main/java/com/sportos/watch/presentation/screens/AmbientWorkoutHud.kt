package com.sportos.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.sportos.watch.core.util.FormatUtils
import com.sportos.watch.sports.SportEngineState
import com.sportos.watch.sports.basketball.BasketballState
import com.sportos.watch.sports.cricket.CricketState
import com.sportos.watch.sports.football.FootballState
import com.sportos.watch.sports.running.RunningState
import com.sportos.watch.sports.tennis.TennisState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * State representing Wear OS ambient mode lifecycle and hardware capabilities.
 */
@Immutable
data class AmbientState(
    val isAmbient: Boolean = false,
    val burnInProtectionRequired: Boolean = false,
    val deviceHasLowBitAmbient: Boolean = false
)

/**
 * Ultra-low-power Wear OS Ambient Mode HUD for Calopeia (Always-On Display / AOD).
 *
 * Characteristics:
 * 1. 100% True Black (#000000) background: Turns off OLED pixels completely (0 mW per dark pixel).
 * 2. High-contrast monochrome typography: White/muted gray outline text; zero color subpixel draw.
 * 3. Anti-burn-in protection: Shifts pixel coordinates periodically when burnInProtectionRequired is active.
 * 4. Zero animation frames, no pager scroll, no map rendering, no continuous redraw loops.
 */
@Composable
fun AmbientWorkoutHud(
    sportType: String,
    state: SportEngineState?,
    ambientState: AmbientState,
    modifier: Modifier = Modifier
) {
    // Current time of day formatted cleanly
    val timeOfDay = remember(System.currentTimeMillis() / 60_000L) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    // Wear OS Anti-Burn-In pixel shifting (moves content by -2dp to +2dp every minute)
    val currentMinute = (System.currentTimeMillis() / 60_000L).toInt()
    val burnInOffsetX = if (ambientState.burnInProtectionRequired) {
        ((currentMinute % 5) - 2).dp
    } else {
        0.dp
    }
    val burnInOffsetY = if (ambientState.burnInProtectionRequired) {
        (((currentMinute / 5) % 5) - 2).dp
    } else {
        0.dp
    }

    val elapsedDuration = FormatUtils.fastFormatDuration(state?.elapsedTimeMs ?: 0L)
    val heartRateStr = if (state != null && state.activeHeartRate > 0) {
        "${state.activeHeartRate.toInt()}"
    } else {
        "--"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = burnInOffsetX, y = burnInOffsetY)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Time of Day (Subtle ambient header)
            Text(
                text = timeOfDay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFAAAAAA),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Main Elapsed Workout Time (Monospace/Tabular digits)
            Text(
                text = elapsedDuration,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Heart Rate (Monochrome AOD)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "♥",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "$heartRateStr BPM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Sport-Specific Primary Telemetry
            AmbientSportMetricRow(sportType = sportType, state = state)

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Low-power Ambient Mode indicator
            Text(
                text = if (state?.isPaused == true) "PAUSED" else "AOD • TILT TO WAKE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF666666),
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun AmbientSportMetricRow(
    sportType: String,
    state: SportEngineState?
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            sportType.equals("Running", ignoreCase = true) -> {
                val runningState = state as? RunningState
                val distStr = FormatUtils.fastFormat2Dec((runningState?.distanceMeters ?: 0.0) / 1000.0)
                val paceStr = FormatUtils.fastFormatPace(runningState?.currentPaceMinPerKm ?: 0.0)

                Text(
                    text = "$distStr km",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "  |  ",
                    fontSize = 11.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "$paceStr/km",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
            sportType.equals("Basketball", ignoreCase = true) -> {
                val bState = state as? BasketballState
                val jumpCount = bState?.jumpCount ?: 0
                val maxJump = FormatUtils.fastFormat1Dec(bState?.maxJumpHeightInches ?: 0.0)

                Text(
                    text = "$jumpCount Jumps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "  |  ",
                    fontSize = 11.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "Max $maxJump\"",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
            sportType.equals("Football", ignoreCase = true) -> {
                val fState = state as? FootballState
                val topSpeed = FormatUtils.fastFormat1Dec(fState?.topSpeedKmh ?: 0.0)
                val distM = (fState?.distanceMeters ?: 0.0).toInt()

                Text(
                    text = "Top $topSpeed km/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "  |  ",
                    fontSize = 11.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "$distM m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
            sportType.equals("Cricket", ignoreCase = true) -> {
                val cState = state as? CricketState
                val balls = cState?.totalBallsBowled ?: 0
                val armSpeed = FormatUtils.fastFormat1Dec(cState?.lastDeliverySpeedKmh ?: 0.0)

                Text(
                    text = "$balls Balls",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "  |  ",
                    fontSize = 11.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "$armSpeed km/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
            sportType.equals("Tennis", ignoreCase = true) -> {
                val tState = state as? TennisState
                val strokes = tState?.swingCount ?: 0
                val racketSpeed = FormatUtils.fastFormat1Dec(tState?.lastRacketSpeedKmh ?: 0.0)

                Text(
                    text = "$strokes Hits",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "  |  ",
                    fontSize = 11.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "$racketSpeed km/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
            else -> {
                val cals = (state?.caloriesKcal ?: 0.0).toInt()
                Text(
                    text = "$cals kcal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEEEEEE),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
