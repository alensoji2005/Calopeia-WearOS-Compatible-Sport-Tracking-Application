package com.sportos.watch.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Switch
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.sportos.watch.presentation.components.StravaOrange
import com.sportos.watch.presentation.theme.CalopeiaAmber
import com.sportos.watch.presentation.theme.CalopeiaCardBorder
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaCrimsonBright
import com.sportos.watch.presentation.theme.CalopeiaNeonGreen
import com.sportos.watch.presentation.theme.CalopeiaPureBlack
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite
import com.sportos.watch.presentation.theme.SportIcon
import com.sportos.watch.presentation.theme.SportIconType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainMenuScreen(
    onSportSelected: (sportType: String, subMode: String, targetPace: Double, isDemo: Boolean) -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    var isDemoMode by remember { mutableStateOf(true) }
    var selectedSportForSubMode by remember { mutableStateOf<String?>(null) }

    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalopeiaPureBlack)
    ) {
        if (selectedSportForSubMode != null) {
            val sport = selectedSportForSubMode!!
            val subModeColumnState = rememberResponsiveColumnState()

        ScreenScaffold(scrollState = subModeColumnState) {
            ScalingLazyColumn(
                columnState = subModeColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    ) {
                        Text(
                            text = "SELECT MODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = CalopeiaCrimson,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = sport.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = CalopeiaTextWhite
                        )
                    }
                }

                when (sport) {
                    "Running" -> {
                        item {
                            SportModeOptionCard(
                                title = "Ghost Pacer (5:00/km)",
                                subtitle = "Real-time avatar duel & pace delta",
                                accentColor = CalopeiaCrimson,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "GHOST_5_00", 5.0, isDemoMode)
                                }
                            )
                        }
                        item {
                            SportModeOptionCard(
                                title = "Fast Pacer (4:30/km)",
                                subtitle = "High tempo ghost benchmark",
                                accentColor = CalopeiaAmber,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "GHOST_4_30", 4.5, isDemoMode)
                                }
                            )
                        }
                        item {
                            SportModeOptionCard(
                                title = "Free Run (No Pacer)",
                                subtitle = "Open distance & GPS tracking",
                                accentColor = CalopeiaTextMuted,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "FREE_RUN", 0.0, isDemoMode)
                                }
                            )
                        }
                    }
                    "Basketball" -> {
                        item {
                            SportModeOptionCard(
                                title = "5v5 Game Mode",
                                subtitle = "Automatic jump & shot kinematics",
                                accentColor = CalopeiaCrimson,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "GAME_AUTOMATIC", 5.0, isDemoMode)
                                }
                            )
                        }
                        item {
                            SportModeOptionCard(
                                title = "Practice Shot Logger",
                                subtitle = "Tactile Made / Missed shot scoring",
                                accentColor = CalopeiaNeonGreen,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "PRACTICE_DRILL", 5.0, isDemoMode)
                                }
                            )
                        }
                    }
                    "Cricket" -> {
                        item {
                            SportModeOptionCard(
                                title = "Bowling Spell",
                                subtitle = "Overs, ball speed & delivery tracker",
                                accentColor = CalopeiaCrimson,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "BOWLING", 5.0, isDemoMode)
                                }
                            )
                        }
                        item {
                            SportModeOptionCard(
                                title = "Batting Mode",
                                subtitle = "Running between wickets & burst rate",
                                accentColor = CalopeiaAmber,
                                onClick = {
                                    selectedSportForSubMode = null
                                    onSportSelected(sport, "BATTING", 5.0, isDemoMode)
                                }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { selectedSportForSubMode = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CalopeiaCardDark,
                            contentColor = CalopeiaTextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text("← Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
                columnState = columnState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            ) {
                // Brand Header
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CalopeiaCrimson)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "CALOPEIA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CalopeiaTextWhite,
                                letterSpacing = 2.sp
                            )
                        }
                        Text(
                            text = "PRO SPORTS OS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalopeiaCrimsonBright,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // 1. RUNNING (Strava / Ghost Runner)
                item {
                    SportLauncherCard(
                        iconType = SportIconType.RUNNING,
                        iconTint = StravaOrange,
                        title = "RUNNING",
                        subtitle = "Strava GPS • Splits • PR Duel",
                        badge = "STRAVA ENGINE",
                        badgeColor = StravaOrange,
                        onClick = {
                            selectedSportForSubMode = "Running"
                        }
                    )
                }

                // 2. BASKETBALL (Dual Mode: Game vs Practice)
                item {
                    SportLauncherCard(
                        iconType = SportIconType.BASKETBALL,
                        iconTint = CalopeiaAmber,
                        title = "BASKETBALL",
                        subtitle = "5v5 Auto Game • Shot Logger Drill",
                        badge = "KINEMATIC IMU",
                        badgeColor = CalopeiaAmber,
                        onClick = {
                            selectedSportForSubMode = "Basketball"
                        }
                    )
                }

                // 3. FOOTBALL (Soccer)
                item {
                    SportLauncherCard(
                        iconType = SportIconType.FOOTBALL,
                        iconTint = CalopeiaCrimsonBright,
                        title = "FOOTBALL",
                        subtitle = "Match Halves • Sprints • Speedometer",
                        badge = "MATCH HUD",
                        badgeColor = CalopeiaCrimsonBright,
                        onClick = {
                            onSportSelected("Football", "", 5.0, isDemoMode)
                        }
                    )
                }

                // 4. CRICKET (Dual Mode: Bowling vs Batting)
                item {
                    SportLauncherCard(
                        iconType = SportIconType.CRICKET,
                        iconTint = CalopeiaCrimson,
                        title = "CRICKET",
                        subtitle = "Bowling Spells • Arm Speed • Batting",
                        badge = "SPELL TRACKER",
                        badgeColor = CalopeiaCrimson,
                        onClick = {
                            selectedSportForSubMode = "Cricket"
                        }
                    )
                }

                // 5. TENNIS (Rally & Strokes)
                item {
                    SportLauncherCard(
                        iconType = SportIconType.TENNIS,
                        iconTint = Color(0xFFCDDC39),
                        title = "TENNIS",
                        subtitle = "Stroke Classifier • Rally Timer",
                        badge = "AI STROKES",
                        badgeColor = Color(0xFFCDDC39),
                        onClick = {
                            onSportSelected("Tennis", "", 5.0, isDemoMode)
                        }
                    )
                }

                // 6. WORKOUT HISTORY & PRs
                item {
                    SportLauncherCard(
                        iconType = SportIconType.TROPHY_PR,
                        iconTint = CalopeiaAmber,
                        title = "HISTORY & PRs",
                        subtitle = "Saved workouts • Personal bests",
                        badge = "ROOM DB",
                        badgeColor = CalopeiaAmber,
                        onClick = onNavigateToHistory
                    )
                }

                // Demo Simulation Toggle Chip
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .background(Color(0xFF101014), RoundedCornerShape(12.dp))
                            .border(0.5.dp, Color(0xFF262630), RoundedCornerShape(12.dp))
                            .clickable { isDemoMode = !isDemoMode }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SIMULATION ENGINE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDemoMode) CalopeiaNeonGreen else CalopeiaTextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (isDemoMode) "Live Demo Data ON" else "Real Hardware Only",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CalopeiaTextWhite
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (isDemoMode) CalopeiaNeonGreen else Color(0xFF555555))
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun SportModeOptionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFF101014), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color(0xFF262630), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = CalopeiaTextMuted
            )
        }
    }
}

@Composable
private fun SportLauncherCard(
    iconType: SportIconType,
    iconTint: Color = CalopeiaCrimson,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color = CalopeiaCrimsonBright,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFF101014), RoundedCornerShape(14.dp))
            .border(0.5.dp, Color(0xFF262630), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(0.5.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SportIcon(
                    type = iconType,
                    size = 18.dp,
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = CalopeiaTextWhite,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = CalopeiaTextMuted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

