package com.sportos.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.sportos.watch.presentation.theme.CalopeiaCardBorder
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaNeonGreen
import com.sportos.watch.presentation.theme.CalopeiaPureBlack
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite
import com.sportos.watch.sports.SportEngineState
import com.sportos.watch.sports.basketball.BasketballState
import com.sportos.watch.sports.cricket.CricketState
import com.sportos.watch.sports.football.FootballState
import com.sportos.watch.sports.running.RunningState
import com.sportos.watch.sports.tennis.TennisState

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.sportos.watch.core.export.GpxExporter
import com.sportos.watch.core.export.TrackPoint
import com.sportos.watch.core.haptics.SportHapticManager
import com.sportos.watch.data.repository.WorkoutRepositoryImpl
import com.sportos.watch.domain.repository.WorkoutRepository
import com.sportos.watch.presentation.theme.SportIcon
import com.sportos.watch.presentation.theme.SportIconType
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun WorkoutSummaryScreen(
    sportType: String,
    finalState: SportEngineState?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { SportHapticManager(context) }
    val repository = remember { WorkoutRepositoryImpl(context) }
    var savedSessionId by remember { mutableStateOf<Long?>(null) }
    var shareSuccessMsg by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(finalState) {
        if (finalState != null && savedSessionId == null) {
            try {
                val id = repository.saveWorkoutSession(
                    sportType = sportType,
                    subMode = "",
                    state = finalState
                )
                savedSessionId = id
            } catch (_: Exception) {}
        }
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(CalopeiaPureBlack)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "WORKOUT COMPLETE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaCrimson,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = sportType.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalopeiaTextWhite
                    )
                }
            }

            // Duration & Calories Card
            item {
                val elapsedMs = finalState?.elapsedTimeMs ?: 0L
                val timeStr = formatSummaryTime(elapsedMs)
                val cals = finalState?.caloriesKcal?.toInt() ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        label = "DURATION",
                        value = timeStr,
                        accentColor = CalopeiaTextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        label = "CALORIES",
                        value = "$cals",
                        unit = "kcal",
                        accentColor = CalopeiaCrimson,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Heart Rate Card
            item {
                val hr = finalState?.activeHeartRate?.toInt() ?: 0
                SummaryStatCard(
                    label = "AVERAGE HEART RATE",
                    value = "$hr",
                    unit = "bpm",
                    accentColor = CalopeiaNeonGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Sport Specific Highlights
            when (finalState) {
                is RunningState -> {
                    item {
                        val km = String.format("%.2f", finalState.distanceMeters / 1000.0)
                        val pace = formatPace(finalState.averagePaceMinPerKm)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatCard(
                                label = "DISTANCE",
                                value = km,
                                unit = "km",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                label = "AVG PACE",
                                value = pace,
                                unit = "/km",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        val lapsCount = finalState.laps.size
                        SummaryStatCard(
                            label = "LAPS COMPLETED",
                            value = "$lapsCount",
                            accentColor = CalopeiaTextWhite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                is BasketballState -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatCard(
                                label = "TOTAL JUMPS",
                                value = "${finalState.jumpCount}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                label = "MAX VERTICAL",
                                value = String.format("%.1f", finalState.maxJumpHeightInches),
                                unit = "in",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        val pLoad = String.format("%.1f", finalState.playerLoad)
                        val fgStr = if (finalState.shotsAttempted > 0) {
                            "${finalState.shotsMade}/${finalState.shotsAttempted} (${finalState.shootingPercentage.toInt()}%)"
                        } else {
                            "$pLoad PL"
                        }
                        SummaryStatCard(
                            label = if (finalState.shotsAttempted > 0) "SHOOTING ACCURACY" else "PLAYER LOAD",
                            value = fgStr,
                            accentColor = CalopeiaCrimson,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                is FootballState -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatCard(
                                label = "SPRINTS",
                                value = "${finalState.sprintCount}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                label = "TOP SPEED",
                                value = String.format("%.1f", finalState.topSpeedKmh),
                                unit = "km/h",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        val distKm = String.format("%.2f", finalState.distanceMeters / 1000.0)
                        SummaryStatCard(
                            label = "DISTANCE COVERED",
                            value = distKm,
                            unit = "km",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                is CricketState -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatCard(
                                label = "OVERS",
                                value = finalState.overDisplay,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                label = "TOP SPEED",
                                value = String.format("%.1f", finalState.topDeliverySpeedKmh),
                                unit = "km/h",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is TennisState -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatCard(
                                label = "TOTAL SWINGS",
                                value = "${finalState.swingCount}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatCard(
                                label = "TOP SERVE",
                                value = String.format("%.1f", finalState.maxRacketSpeedKmh),
                                unit = "km/h",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Sync Status
            item {
                Text(
                    text = if (savedSessionId != null) "✓ Session Saved to Database (#$savedSessionId)" else "Saving session to storage...",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = CalopeiaNeonGreen,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Pre-built Android Share Sheet & GPX Export
            item {
                Button(
                    onClick = {
                        haptic.heavyClick()
                        try {
                            if (finalState is RunningState && finalState.routePoints.isNotEmpty()) {
                                val trackPoints = finalState.routePoints.map { pt ->
                                    TrackPoint(
                                        latitude = pt.latitude,
                                        longitude = pt.longitude,
                                        elevationMeters = pt.altitudeMeters,
                                        timeMs = pt.timestampMs,
                                        heartRate = finalState.activeHeartRate.toInt(),
                                        cadence = finalState.cadenceSpm.toInt()
                                    )
                                }
                                val gpxFile = GpxExporter(context).exportToGpx(sportType, trackPoints)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    gpxFile
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/gpx+xml"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_SUBJECT, "$sportType Workout GPX - Calopeia")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export GPX Route"))
                                shareSuccessMsg = "✓ GPX Export Initiated"
                            } else {
                                val summaryText = buildString {
                                    appendLine("Calopeia Workout Summary")
                                    appendLine("Sport: $sportType")
                                    appendLine("Duration: ${formatSummaryTime(finalState?.elapsedTimeMs ?: 0L)}")
                                    appendLine("Calories: ${finalState?.caloriesKcal?.toInt() ?: 0} kcal")
                                    appendLine("Avg HR: ${finalState?.activeHeartRate?.toInt() ?: 0} bpm")
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, summaryText)
                                    putExtra(Intent.EXTRA_SUBJECT, "$sportType Workout Summary")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Workout"))
                                shareSuccessMsg = "✓ Summary Shared"
                            }
                        } catch (e: Exception) {
                            shareSuccessMsg = "Export: ${e.message}"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1C1C24),
                        contentColor = CalopeiaTextWhite
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SportIcon(
                            type = SportIconType.SHARE_EXPORT,
                            size = 13.dp,
                            tint = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (finalState is RunningState) "EXPORT GPX" else "SHARE STATS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (shareSuccessMsg != null) {
                item {
                    Text(
                        text = shareSuccessMsg!!,
                        fontSize = 9.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Exit Button
            item {
                Button(
                    onClick = {
                        haptic.click()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCrimson,
                        contentColor = CalopeiaTextWhite
                    )
                ) {
                    Text(
                        text = "DONE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    unit: String? = null,
    accentColor: Color = CalopeiaTextWhite,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CalopeiaCardDark, RoundedCornerShape(12.dp))
            .border(1.dp, CalopeiaCardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = CalopeiaTextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = if (value.length > 5) 14.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1
            )
            if (unit != null) {
                Text(
                    text = " $unit",
                    fontSize = 9.sp,
                    color = CalopeiaTextMuted,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

private fun formatSummaryTime(ms: Long): String {
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatPace(paceMinPerKm: Double): String {
    if (paceMinPerKm <= 0.1 || paceMinPerKm > 30) return "--'--\""
    val minutes = paceMinPerKm.toInt()
    val seconds = ((paceMinPerKm - minutes) * 60).toInt()
    return String.format("%d'%02d\"", minutes, seconds)
}
