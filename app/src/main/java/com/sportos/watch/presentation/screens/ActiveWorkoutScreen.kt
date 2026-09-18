package com.sportos.watch.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.sportos.watch.presentation.components.BezelHeartRateArc
import com.sportos.watch.presentation.components.GpsMapView
import com.sportos.watch.presentation.components.MetricTile
import com.sportos.watch.presentation.components.PrebuiltMapView
import com.sportos.watch.presentation.components.StravaOrange
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import com.sportos.watch.core.haptics.SportHapticManager
import com.sportos.watch.core.audio.SportAudioToneManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.sportos.watch.presentation.theme.CalopeiaAmber
import com.sportos.watch.presentation.theme.CalopeiaCardBorder
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaCrimsonBright
import com.sportos.watch.presentation.theme.CalopeiaNeonGreen
import com.sportos.watch.presentation.theme.CalopeiaPureBlack
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite
import com.sportos.watch.presentation.theme.HrZones
import com.sportos.watch.presentation.theme.SportIcon
import com.sportos.watch.presentation.theme.SportIconType
import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.SportEngineState
import com.sportos.watch.sports.basketball.BasketballEngine
import com.sportos.watch.sports.basketball.BasketballMode
import com.sportos.watch.sports.basketball.BasketballState
import com.sportos.watch.sports.cricket.CricketEngine
import com.sportos.watch.sports.cricket.CricketMode
import com.sportos.watch.sports.cricket.CricketState
import com.sportos.watch.sports.football.FootballEngine
import com.sportos.watch.sports.football.FootballState
import com.sportos.watch.sports.running.LapSplit
import com.sportos.watch.sports.running.RunningEngine
import com.sportos.watch.sports.running.RunningState
import com.sportos.watch.sports.tennis.TennisEngine
import com.sportos.watch.sports.tennis.TennisState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveWorkoutScreen(
    sportType: String,
    engineState: SportEngineState?,
    activeEngine: SportEngine?,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onLapTrigger: () -> Unit,
    onFinishWorkout: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { SportHapticManager(context) }
    val audioTone = remember { SportAudioToneManager() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val isRunning = sportType.equals("Running", ignoreCase = true)
    val pageCount = if (isRunning) 5 else 4
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }
    var showEndConfirmation by remember { mutableStateOf(false) }

    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            audioTone.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalopeiaPureBlack)
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent { event ->
                // Map page (page 1 on running) handles its own rotary zoom
                if (isRunning && pagerState.currentPage == 1) {
                    false
                } else {
                    haptic.rotaryTick()
                    if (event.verticalScrollPixels > 0) {
                        if (pagerState.currentPage < pageCount - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                        true
                    } else if (event.verticalScrollPixels < 0) {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                        true
                    } else false
                }
            }
    ) {
        // Outer Bezel Heart Rate Arc on the primary telemetry screen
        if (pagerState.currentPage == 0 && engineState != null) {
            BezelHeartRateArc(heartRateBpm = engineState.activeHeartRate)
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (isRunning) {
                val runState = engineState as? RunningState
                when (page) {
                    0 -> StravaRunningHudPage(
                        state = runState,
                        clockTime = currentTime
                    )
                    1 -> StravaMapPage(
                        state = runState,
                        isVisible = (pagerState.currentPage == 1)
                    )
                    2 -> StravaSegmentPage(
                        state = runState
                    )
                    3 -> StravaSplitsPage(
                        state = runState
                    )
                    4 -> WorkoutControlsPage(
                        isPaused = engineState?.isPaused ?: false,
                        sportType = sportType,
                        onPause = {
                            haptic.click()
                            onPauseWorkout()
                        },
                        onResume = {
                            haptic.click()
                            audioTone.playStartAlert()
                            onResumeWorkout()
                        },
                        onLap = {
                            haptic.doubleClick()
                            audioTone.playMilestoneChime()
                            onLapTrigger()
                        },
                        onRequestEnd = { showEndConfirmation = true },
                        haptic = haptic
                    )
                }
            } else {
                when (page) {
                    0 -> PrimaryTelemetryPage(
                        sportType = sportType,
                        state = engineState,
                        clockTime = currentTime
                    )
                    1 -> SportTacticalPage(
                        sportType = sportType,
                        state = engineState,
                        engine = activeEngine,
                        haptic = haptic,
                        audioTone = audioTone
                    )
                    2 -> WorkoutControlsPage(
                        isPaused = engineState?.isPaused ?: false,
                        sportType = sportType,
                        onPause = {
                            haptic.click()
                            onPauseWorkout()
                        },
                        onResume = {
                            haptic.click()
                            audioTone.playStartAlert()
                            onResumeWorkout()
                        },
                        onLap = {
                            haptic.doubleClick()
                            audioTone.playMilestoneChime()
                            onLapTrigger()
                        },
                        onRequestEnd = { showEndConfirmation = true },
                        haptic = haptic
                    )
                    3 -> DeviceHealthPage(
                        state = engineState
                    )
                }
            }
        }

        // Curved Page Indicator Dots (Bottom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) (if (isRunning) StravaOrange else CalopeiaCrimson) else Color(0xFF404045))
                )
            }
        }

        // Accidental-Touch Protection End Confirmation Sheet
        AnimatedVisibility(
            visible = showEndConfirmation,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "FINISH WORKOUT?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaTextWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Session will be saved & synced.",
                        fontSize = 10.sp,
                        color = CalopeiaTextMuted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.click()
                                showEndConfirmation = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CalopeiaCardDark,
                                contentColor = CalopeiaTextWhite
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Resume", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                haptic.workoutFinished()
                                showEndConfirmation = false
                                onFinishWorkout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CalopeiaCrimson,
                                contentColor = CalopeiaTextWhite
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Finish", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PAGE 0: PRIMARY TELEMETRY HUD
// -------------------------------------------------------------
@Composable
private fun PrimaryTelemetryPage(
    sportType: String,
    state: SportEngineState?,
    clockTime: String
) {
    val elapsedMs = state?.elapsedTimeMs ?: 0L
    val hr = state?.activeHeartRate ?: 0.0
    val zoneColor = HrZones.getZoneColor(hr)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: Centered Sport Pill & GPS Indicator (Optimized for Round Screens)
        Row(
            modifier = Modifier
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportIcon(
                type = SportIconType.GPS_SATELLITE,
                size = 11.dp,
                tint = CalopeiaNeonGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "GPS 3D",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaNeonGreen
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "•",
                fontSize = 9.sp,
                color = CalopeiaTextMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = sportType.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaCrimson,
                letterSpacing = 1.sp
            )
        }

        // Center Primary Readout: Giant Elapsed Time & Sub-metric
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDuration(elapsedMs),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaTextWhite,
                letterSpacing = (-0.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(zoneColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${hr.toInt()} BPM",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = zoneColor
                )
                if (state?.isPaused == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• PAUSED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaAmber
                    )
                }
            }
        }

        // Bottom 2x2 Metric Grid tailored to Sport
        when (state) {
            is RunningState -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricTile(
                        label = "Pace",
                        value = formatPace(state.currentPaceMinPerKm),
                        unit = "/km",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Dist",
                        value = String.format("%.2f", state.distanceMeters / 1000.0),
                        unit = "km",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Cadence",
                        value = "${state.cadenceSpm.toInt()}",
                        unit = "spm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is BasketballState -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricTile(
                        label = "Jumps",
                        value = "${state.jumpCount}",
                        valueColor = CalopeiaCrimson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Vert",
                        value = String.format("%.1f", state.maxJumpHeightInches),
                        unit = "in",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Load",
                        value = String.format("%.1f", state.playerLoad),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is FootballState -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricTile(
                        label = "Speed",
                        value = String.format("%.1f", state.currentSpeedKmh),
                        unit = "kmh",
                        valueColor = if (state.currentSpeedKmh > 20) CalopeiaCrimson else CalopeiaTextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Sprints",
                        value = "${state.sprintCount}",
                        valueColor = CalopeiaAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Dist",
                        value = String.format("%.2f", state.distanceMeters / 1000.0),
                        unit = "km",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is CricketState -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricTile(
                        label = "Overs",
                        value = state.overDisplay,
                        valueColor = CalopeiaTextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Arm",
                        value = String.format("%.1f", state.lastDeliverySpeedKmh),
                        unit = "kmh",
                        valueColor = CalopeiaCrimson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Sprints",
                        value = "${state.burstSprintsCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is TennisState -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricTile(
                        label = "Swings",
                        value = "${state.swingCount}",
                        valueColor = CalopeiaCrimson,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Speed",
                        value = String.format("%.1f", state.lastRacketSpeedKmh),
                        unit = "kmh",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Serves",
                        value = "${state.serves}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Syncing sensor telemetry...",
                        fontSize = 11.sp,
                        color = CalopeiaTextMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

// -------------------------------------------------------------
// PAGE 1: DEEP SPORT-SPECIFIC TACTICAL MODE
// -------------------------------------------------------------
@Composable
private fun SportTacticalPage(
    sportType: String,
    state: SportEngineState?,
    engine: SportEngine?,
    haptic: SportHapticManager,
    audioTone: SportAudioToneManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            is RunningState -> GhostRunnerTacticalView(state)
            is BasketballState -> BasketballTacticalView(state, engine as? BasketballEngine, haptic, audioTone)
            is FootballState -> FootballTacticalView(state, engine as? FootballEngine, haptic, audioTone)
            is CricketState -> CricketTacticalView(state, engine as? CricketEngine)
            is TennisState -> TennisTacticalView(state, engine as? TennisEngine)
            else -> {
                Text(
                    text = "Sport Tactical Center",
                    color = CalopeiaTextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GhostRunnerTacticalView(state: RunningState) {
    val isAhead = state.targetPaceDeltaMs >= 0
    val deltaSec = Math.abs(state.targetPaceDeltaMs) / 1000
    val deltaDistM = Math.abs(state.targetDistanceDeltaMeters).toInt()
    val deltaColor = if (isAhead) CalopeiaNeonGreen else CalopeiaCrimsonBright

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "GHOST RUNNER DUEL",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaTextMuted,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        // Big Delta Banner
        Box(
            modifier = Modifier
                .background(deltaColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, deltaColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isAhead) "▲ +${deltaSec}s AHEAD" else "▼ -${deltaSec}s BEHIND",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = deltaColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Gap: ${deltaDistM}m • Target: ${formatPace(state.targetPaceMinPerKm)}",
            fontSize = 11.sp,
            color = CalopeiaTextWhite,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Linear Track Duel Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(CalopeiaCardDark, RoundedCornerShape(9.dp))
                .border(1.dp, CalopeiaCardBorder, RoundedCornerShape(9.dp))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Target Pacer (Ghost) Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(8.dp)
                    .background(Color(0xFF606068), RoundedCornerShape(4.dp))
            )
            // User Marker
            val userFraction = (0.5f + (state.targetDistanceDeltaMeters / 200.0).toFloat()).coerceIn(0.1f, 0.9f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(userFraction)
                    .height(8.dp)
                    .background(deltaColor, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Lap ${state.currentLapNumber} • Split ${formatDuration(state.currentLapTimeMs)}",
            fontSize = 10.sp,
            color = CalopeiaTextMuted
        )
    }
}

@Composable
private fun BasketballTacticalView(
    state: BasketballState,
    engine: BasketballEngine?,
    haptic: SportHapticManager,
    audioTone: SportAudioToneManager
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val isPractice = state.mode == BasketballMode.PRACTICE_DRILL
        Text(
            text = if (isPractice) "SHOT LOGGER • PRACTICE" else "5v5 GAME AUTOMATIC",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaCrimson,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isPractice) {
            // Tactile Shot Logger Buttons: [ MADE + ] & [ MISSED - ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.heavyClick()
                        audioTone.playMilestoneChime()
                        engine?.logMadeShot()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaNeonGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ MADE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        haptic.shotMissed()
                        engine?.logMissedShot()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCrimson,
                        contentColor = CalopeiaTextWhite
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("- MISS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Shooting Percentage & Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "${state.shotsMade}/${state.shotsAttempted} (${state.shootingPercentage.toInt()}%)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = CalopeiaTextWhite
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SportIcon(
                        type = SportIconType.FLAME_STREAK,
                        size = 13.dp,
                        tint = CalopeiaAmber
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Streak: ${state.currentStreak}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalopeiaAmber
                    )
                }
            }
        } else {
            // Game Automatic Kinematics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTile(
                    label = "Last Jump",
                    value = String.format("%.1f", state.lastJumpHeightInches),
                    unit = "in",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Hang Time",
                    value = String.format("%.2f", state.lastHangTimeMs / 1000.0),
                    unit = "s",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTile(
                    label = "PlayerLoad",
                    value = String.format("%.1f", state.playerLoad),
                    valueColor = CalopeiaAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Quarter",
                    value = "Q${state.quarter}",
                    valueColor = CalopeiaTextWhite,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FootballTacticalView(
    state: FootballState,
    engine: FootballEngine?,
    haptic: SportHapticManager,
    audioTone: SportAudioToneManager
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MATCH COMMAND",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaCrimson,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Match Period Badge & Advance Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(CalopeiaCardDark, RoundedCornerShape(8.dp))
                    .border(1.dp, CalopeiaCardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = state.period.label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CalopeiaAmber
                )
            }
            Button(
                onClick = {
                    haptic.whistlePattern()
                    audioTone.playRefereeWhistle()
                    engine?.advancePeriod()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CalopeiaCrimson,
                    contentColor = CalopeiaTextWhite
                ),
                modifier = Modifier.height(28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SportIcon(
                        type = SportIconType.WHISTLE,
                        size = 11.dp,
                        tint = CalopeiaTextWhite
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Whistle", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricTile(
                label = "Top Speed",
                value = String.format("%.1f", state.topSpeedKmh),
                unit = "kmh",
                valueColor = CalopeiaCrimson,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "HIRD (>15)",
                value = String.format("%.0f", state.highIntensityDistanceMeters),
                unit = "m",
                valueColor = CalopeiaNeonGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CricketTacticalView(state: CricketState, engine: CricketEngine?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val isBowling = state.mode == CricketMode.BOWLING
        Text(
            text = if (isBowling) "BOWLING SPELL COMMAND" else "BATTING WORKLOAD",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaCrimson,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isBowling) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { engine?.logDelivery() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCrimson,
                        contentColor = CalopeiaTextWhite
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ BALL", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                Button(
                    onClick = { engine?.logWicket() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaNeonGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WICKET", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTile(
                    label = "Over",
                    value = state.overDisplay,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Top Arm",
                    value = String.format("%.1f", state.topDeliverySpeedKmh),
                    unit = "kmh",
                    valueColor = CalopeiaCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MetricTile(
                label = "Wicket Sprints",
                value = "${state.burstSprintsCount}",
                unit = "bursts",
                valueColor = CalopeiaAmber,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TennisTacticalView(state: TennisState, engine: TennisEngine?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "STROKE INTELLIGENCE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaCrimson,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Rally Button
        Button(
            onClick = {
                if (state.isRallyActive) engine?.endRally() else engine?.startRally()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isRallyActive) CalopeiaAmber else CalopeiaNeonGreen,
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (state.isRallyActive) "END RALLY (${state.activeRallyDurationMs / 1000}s)" else "START RALLY",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricTile(label = "Fore", value = "${state.forehands}", modifier = Modifier.weight(1f))
            MetricTile(label = "Back", value = "${state.backhands}", modifier = Modifier.weight(1f))
            MetricTile(label = "Serve", value = "${state.serves}", modifier = Modifier.weight(1f))
        }
    }
}

// -------------------------------------------------------------
// WORKOUT CONTROLS & ACTIONS
// -------------------------------------------------------------
@Composable
private fun WorkoutControlsPage(
    isPaused: Boolean,
    sportType: String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onLap: () -> Unit,
    onRequestEnd: () -> Unit,
    haptic: SportHapticManager
) {
    var isWaterLocked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 18.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WORKOUT CONTROLS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaTextMuted,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pause / Resume Button
        Button(
            onClick = {
                haptic.click()
                if (isPaused) onResume() else onPause()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPaused) CalopeiaNeonGreen else CalopeiaAmber,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = if (isPaused) "RESUME" else "PAUSE",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Lap / Split Trigger Button
            Button(
                onClick = {
                    haptic.doubleClick()
                    onLap()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF181820),
                    contentColor = CalopeiaTextWhite
                )
            ) {
                Text(
                    text = "LAP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Water Lock Button
            Button(
                onClick = {
                    haptic.click()
                    isWaterLocked = !isWaterLocked
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWaterLocked) Color(0xFF00E5FF) else Color(0xFF181820),
                    contentColor = if (isWaterLocked) Color.Black else CalopeiaTextWhite
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SportIcon(
                        type = SportIconType.WATER_LOCK,
                        size = 11.dp,
                        tint = if (isWaterLocked) Color.Black else Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isWaterLocked) "LOCKED" else "LOCK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // End Workout Button
        Button(
            onClick = {
                haptic.heavyClick()
                onRequestEnd()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CalopeiaCrimson,
                contentColor = CalopeiaTextWhite
            )
        ) {
            Text(
                text = "FINISH WORKOUT",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

// -------------------------------------------------------------
// STRAVA RUNNING EXPERIENCE (5-PAGE ENGINE)
// -------------------------------------------------------------

@Composable
private fun StravaRunningHudPage(
    state: RunningState?,
    clockTime: String
) {
    val elapsedMs = state?.elapsedTimeMs ?: 0L
    val hr = state?.activeHeartRate ?: 0.0
    val zoneColor = HrZones.getZoneColor(hr)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: Strava Badge & GPS Satellite Status
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportIcon(
                type = SportIconType.GPS_SATELLITE,
                size = 10.dp,
                tint = CalopeiaNeonGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "GPS 3D",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaNeonGreen,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "•",
                fontSize = 8.sp,
                color = CalopeiaTextMuted
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "STRAVA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = StravaOrange,
                letterSpacing = 1.sp
            )
        }

        // Center Primary Readout: Giant Elapsed Time & Big Instantaneous Pace
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDuration(elapsedMs),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaTextWhite,
                letterSpacing = (-0.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatPace(state?.currentPaceMinPerKm ?: 0.0),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = StravaOrange
                )
                Text(
                    text = " /km",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalopeiaTextMuted
                )
                if (state?.isPaused == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• PAUSED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaAmber
                    )
                }
            }
        }

        // Bottom Grid: Distance, Heart Rate, Elevation Gain, Cadence
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                MetricTile(
                    label = "Dist",
                    value = String.format("%.2f", (state?.distanceMeters ?: 0.0) / 1000.0),
                    unit = "km",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "HR",
                    value = "${hr.toInt()}",
                    unit = "bpm",
                    valueColor = zoneColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                MetricTile(
                    label = "Elev Gain",
                    value = "+${state?.elevationGainMeters?.toInt() ?: 0}",
                    unit = "m",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Cadence",
                    value = "${state?.cadenceSpm?.toInt() ?: 0}",
                    unit = "spm",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun StravaMapPage(
    state: RunningState?,
    isVisible: Boolean = true
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val points = state?.routePoints ?: emptyList()
        val distKm = (state?.distanceMeters ?: 0.0) / 1000.0
        val paceStr = formatPace(state?.currentPaceMinPerKm ?: 0.0)

        PrebuiltMapView(
            routePoints = points,
            distanceKm = distKm,
            paceString = paceStr,
            modifier = Modifier.fillMaxSize(),
            isVisible = isVisible
        )
    }
}

@Composable
private fun StravaSegmentPage(
    state: RunningState?
) {
    if (state == null) return

    val isAhead = state.segmentPrDeltaMs <= 0
    val deltaMs = if (state.segmentPrDeltaMs != 0L) Math.abs(state.segmentPrDeltaMs) else Math.abs(state.targetPaceDeltaMs)
    val deltaSec = deltaMs / 1000.0
    val aheadText = if (isAhead) String.format("▲ -%.1fs AHEAD", deltaSec) else String.format("▼ +%.1fs BEHIND", deltaSec)
    val deltaColor = if (isAhead) CalopeiaNeonGreen else CalopeiaCrimsonBright

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SportIcon(
                type = SportIconType.TROPHY_PR,
                size = 13.dp,
                tint = StravaOrange
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "STRAVA LIVE SEGMENT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = StravaOrange,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = state.activeSegmentName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaTextWhite
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Big PR Delta Banner
        Box(
            modifier = Modifier
                .background(deltaColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(0.5.dp, deltaColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = aheadText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = deltaColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.segmentDistanceRemainingM.toInt()}m left",
                fontSize = 10.sp,
                color = CalopeiaTextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "PR: 5:02",
                fontSize = 10.sp,
                color = CalopeiaAmber,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress Bar
        val progress = (1f - (state.segmentDistanceRemainingM.toFloat() / 500f)).coerceIn(0.05f, 0.98f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFF101014), RoundedCornerShape(5.dp))
                .border(0.5.dp, Color(0xFF262630), RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .background(StravaOrange, RoundedCornerShape(5.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Gap: ${Math.abs(state.targetDistanceDeltaMeters).toInt()}m • Target: ${formatPace(state.targetPaceMinPerKm)}",
            fontSize = 9.sp,
            color = CalopeiaTextMuted
        )
    }
}

@Composable
private fun StravaSplitsPage(
    state: RunningState?
) {
    if (state == null) return

    val splits = state.kmSplits
    val activeKm = splits.size + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SportIcon(
                type = SportIconType.MAP_ROUTE,
                size = 11.dp,
                tint = StravaOrange
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "KM SPLITS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = StravaOrange,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101014), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color(0xFF262630), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("KM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextMuted)
                    Text("PACE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextMuted)
                    Text("ELEV", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextMuted)
                    Text("HR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextMuted)
                }

                splits.forEach { split ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${split.lapNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextWhite)
                        Text(formatPace(split.paceMinPerKm), fontSize = 11.sp, fontWeight = FontWeight.Black, color = CalopeiaTextWhite)
                        Text("+${split.elevationGainMeters.toInt()}m", fontSize = 10.sp, color = CalopeiaTextMuted)
                        Text("${split.avgHeartRate.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HrZones.getZoneColor(split.avgHeartRate))
                    }
                }

                // Active Split (in progress)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$activeKm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StravaOrange)
                    Text(formatPace(state.currentPaceMinPerKm), fontSize = 11.sp, fontWeight = FontWeight.Black, color = StravaOrange)
                    Text("+${state.elevationGainMeters.toInt()}m", fontSize = 10.sp, color = CalopeiaTextMuted)
                    Text("${state.activeHeartRate.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HrZones.getZoneColor(state.activeHeartRate))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Total Dist: ${String.format("%.2f", state.distanceMeters / 1000.0)} km",
            fontSize = 9.sp,
            color = CalopeiaTextMuted
        )
    }
}

// -------------------------------------------------------------
// PAGE 3: DEVICE HEALTH & LIVE BEACON
// -------------------------------------------------------------
@Composable
private fun DeviceHealthPage(
    state: SportEngineState?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SYSTEM & TELEMETRY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CalopeiaCrimson,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalopeiaCardDark, RoundedCornerShape(12.dp))
                .border(1.dp, CalopeiaCardBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Live Beacon", fontSize = 11.sp, color = CalopeiaTextMuted)
                    Text("ACTIVE (5s)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CalopeiaNeonGreen)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GPS Sensor", fontSize = 11.sp, color = CalopeiaTextMuted)
                    Text("3D FIX (12 SAT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextWhite)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Battery", fontSize = 11.sp, color = CalopeiaTextMuted)
                    Text("84% • OPTIMAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val cals = state?.caloriesKcal?.toInt() ?: 0
        Text(
            text = "Calories Burned: $cals kcal",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CalopeiaCrimson
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatPace(paceMinPerKm: Double): String {
    if (paceMinPerKm <= 0.1 || paceMinPerKm > 30) return "--'--\""
    val minutes = paceMinPerKm.toInt()
    val seconds = ((paceMinPerKm - minutes) * 60).toInt()
    return String.format("%d'%02d\"", minutes, seconds)
}
