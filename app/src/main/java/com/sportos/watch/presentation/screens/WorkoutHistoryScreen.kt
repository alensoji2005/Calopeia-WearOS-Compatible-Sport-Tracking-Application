package com.sportos.watch.presentation.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.sportos.watch.data.database.entity.PersonalRecordEntity
import com.sportos.watch.data.database.entity.WorkoutSessionEntity
import com.sportos.watch.data.repository.WorkoutRepositoryImpl
import com.sportos.watch.domain.repository.WorkoutRepository
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
fun WorkoutHistoryScreen(
    onNavigateBack: () -> Unit,
    repository: WorkoutRepository = rememberWorkoutRepository()
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val sessions by repository.getAllSessions().collectAsState(initial = emptyList())
    val personalRecords by repository.getAllPRs().collectAsState(initial = emptyList())

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(CalopeiaPureBlack)
                .padding(horizontal = 6.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "RECORDED LOGS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaCrimson,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "WORKOUT HISTORY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalopeiaTextWhite
                    )
                }
            }

            // Milestone Personal Records Section (if any PR is set)
            if (personalRecords.isNotEmpty()) {
                item {
                    Text(
                        text = "PERSONAL BESTS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaAmber,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(personalRecords.size) { index ->
                    val pr = personalRecords[index]
                    PersonalRecordCard(pr = pr)
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Past Sessions Section
            if (sessions.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp)
                    ) {
                        Text(
                            text = "No Workouts Logged",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalopeiaTextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete a workout session to see history & personal bests.",
                            fontSize = 9.sp,
                            color = CalopeiaTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "RECENT SESSIONS (${sessions.size})",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(sessions.size) { index ->
                    val session = sessions[index]
                    WorkoutHistoryCard(session = session)
                }
            }

            // Back Button
            item {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCardDark,
                        contentColor = CalopeiaTextWhite
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
}

@Composable
private fun PersonalRecordCard(pr: PersonalRecordEntity) {
    val sportIcon = when (pr.sportType) {
        "Running" -> SportIconType.RUNNING
        "Basketball" -> SportIconType.BASKETBALL
        "Football" -> SportIconType.FOOTBALL
        "Cricket" -> SportIconType.CRICKET
        "Tennis" -> SportIconType.TENNIS
        else -> SportIconType.TROPHY_PR
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color(0xFF141208), RoundedCornerShape(10.dp))
            .border(0.5.dp, CalopeiaAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CalopeiaAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                SportIcon(type = SportIconType.TROPHY_PR, size = 13.dp, tint = CalopeiaAmber)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pr.title.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalopeiaAmber,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = pr.displayValue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = CalopeiaTextWhite
                )
            }
            Text(
                text = pr.sportType.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = CalopeiaTextMuted
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(session: WorkoutSessionEntity) {
    val (iconType, tint) = when (session.sportType) {
        "Running" -> SportIconType.RUNNING to StravaOrange
        "Basketball" -> SportIconType.BASKETBALL to CalopeiaAmber
        "Football" -> SportIconType.FOOTBALL to CalopeiaCrimsonBright
        "Cricket" -> SportIconType.CRICKET to CalopeiaCrimson
        "Tennis" -> SportIconType.TENNIS to Color(0xFFCDDC39)
        else -> SportIconType.RUNNING to CalopeiaCrimson
    }

    val dateStr = remember(session.startTimeMs) {
        SimpleDateFormat("MMM d • HH:mm", Locale.getDefault()).format(Date(session.startTimeMs))
    }
    val durationStr = remember(session.durationMs) {
        val totalSecs = session.durationMs / 1000
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        val hrs = totalSecs / 3600
        if (hrs > 0) String.format("%02d:%02d:%02d", hrs, mins, secs) else String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(CalopeiaCardDark, RoundedCornerShape(12.dp))
            .border(0.5.dp, CalopeiaCardBorder, RoundedCornerShape(12.dp))
            .padding(9.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        SportIcon(type = iconType, size = 12.dp, tint = tint)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session.sportType.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaTextWhite
                    )
                }
                Text(
                    text = dateStr,
                    fontSize = 8.sp,
                    color = CalopeiaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time: $durationStr",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalopeiaTextWhite
                )
                Text(
                    text = "${session.caloriesKcal.toInt()} kcal",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalopeiaCrimson
                )
                if (session.avgHeartRate > 0) {
                    Text(
                        text = "${session.avgHeartRate.toInt()} bpm",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalopeiaNeonGreen
                    )
                }
            }

            if (session.sportSpecificSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = session.sportSpecificSummary,
                    fontSize = 8.sp,
                    color = CalopeiaTextMuted,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun rememberWorkoutRepository(): WorkoutRepository {
    val context = LocalContext.current
    return remember { WorkoutRepositoryImpl(context) }
}
