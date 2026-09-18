package com.sportos.watch.data.repository

import android.content.Context
import com.sportos.watch.data.database.AppDatabase
import com.sportos.watch.data.database.dao.WorkoutDao
import com.sportos.watch.data.database.entity.PersonalRecordEntity
import com.sportos.watch.data.database.entity.WorkoutSessionEntity
import com.sportos.watch.domain.repository.WorkoutRepository
import com.sportos.watch.sports.SportEngineState
import com.sportos.watch.sports.basketball.BasketballState
import com.sportos.watch.sports.cricket.CricketState
import com.sportos.watch.sports.football.FootballState
import com.sportos.watch.sports.running.RunningState
import com.sportos.watch.sports.tennis.TennisState
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlin.math.max

class WorkoutRepositoryImpl(
    private val dao: WorkoutDao
) : WorkoutRepository {

    constructor(context: Context) : this(AppDatabase.getDatabase(context).workoutDao())

    override suspend fun saveWorkoutSession(
        sportType: String,
        subMode: String,
        state: SportEngineState?
    ): Long {
        val now = System.currentTimeMillis()
        val durationMs = state?.elapsedTimeMs ?: 0L
        val startTime = now - durationMs
        val calories = state?.caloriesKcal ?: 0.0
        val avgHr = state?.activeHeartRate ?: 0.0

        var distance = 0.0
        var avgPace = 0.0
        var summary = ""

        when (state) {
            is RunningState -> {
                distance = state.distanceMeters
                avgPace = state.averagePaceMinPerKm
                val paceStr = formatPaceString(avgPace)
                summary = "Pace: $paceStr • Laps: ${state.laps.size} • Elev: +${state.elevationGainMeters.toInt()}m"

                // PR Check: Longest Run
                if (distance > 500) {
                    checkAndUpdatePR(
                        key = "RUN_MAX_DISTANCE",
                        sport = "Running",
                        title = "Longest Run",
                        value = distance,
                        displayValue = String.format(Locale.US, "%.2f km", distance / 1000.0),
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
            }
            is BasketballState -> {
                val shotsAttempted = state.shotsAttempted
                val fgStr = if (shotsAttempted > 0) "${state.shotsMade}/$shotsAttempted (${state.shootingPercentage.toInt()}%)" else "Auto Game"
                summary = "Jumps: ${state.jumpCount} • Max: ${state.maxJumpHeightInches}\" • FG: $fgStr"

                // PR Check: Max Vertical Jump
                if (state.maxJumpHeightInches > 0) {
                    checkAndUpdatePR(
                        key = "BASKETBALL_MAX_VERTICAL",
                        sport = "Basketball",
                        title = "Max Vertical Jump",
                        value = state.maxJumpHeightInches,
                        displayValue = String.format(Locale.US, "%.1f in", state.maxJumpHeightInches),
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
            }
            is FootballState -> {
                distance = state.distanceMeters
                summary = "Top: ${state.topSpeedKmh}km/h • Sprints: ${state.sprintCount} • HIRD: ${state.highIntensityDistanceMeters.toInt()}m"

                // PR Check: Top Sprint Speed
                if (state.topSpeedKmh > 0) {
                    checkAndUpdatePR(
                        key = "FOOTBALL_TOP_SPEED",
                        sport = "Football",
                        title = "Top Sprint Speed",
                        value = state.topSpeedKmh,
                        displayValue = String.format(Locale.US, "%.1f km/h", state.topSpeedKmh),
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
            }
            is CricketState -> {
                summary = "Overs: ${state.overDisplay} • Top Arm: ${state.topDeliverySpeedKmh}km/h • Wickets: ${state.wicketsTaken}"

                // PR Check: Top Delivery Speed
                if (state.topDeliverySpeedKmh > 0) {
                    checkAndUpdatePR(
                        key = "CRICKET_TOP_ARM_SPEED",
                        sport = "Cricket",
                        title = "Fastest Delivery",
                        value = state.topDeliverySpeedKmh,
                        displayValue = String.format(Locale.US, "%.1f km/h", state.topDeliverySpeedKmh),
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
            }
            is TennisState -> {
                distance = state.distanceMeters
                val rallySec = state.longestRallyDurationMs / 1000
                summary = "Swings: ${state.swingCount} • Top Serve: ${state.maxRacketSpeedKmh}km/h • Rally: ${rallySec}s"

                // PR Check: Top Serve
                if (state.maxRacketSpeedKmh > 0) {
                    checkAndUpdatePR(
                        key = "TENNIS_MAX_SERVE",
                        sport = "Tennis",
                        title = "Fastest Serve",
                        value = state.maxRacketSpeedKmh,
                        displayValue = String.format(Locale.US, "%.1f km/h", state.maxRacketSpeedKmh),
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
                // PR Check: Longest Rally
                if (rallySec > 0) {
                    checkAndUpdatePR(
                        key = "TENNIS_LONGEST_RALLY",
                        sport = "Tennis",
                        title = "Longest Rally",
                        value = rallySec.toDouble(),
                        displayValue = "${rallySec}s",
                        isHigherBetter = true,
                        achievedAt = now
                    )
                }
            }
            else -> {
                summary = "Workout Session Completed"
            }
        }

        val session = WorkoutSessionEntity(
            sportType = sportType,
            subMode = subMode,
            startTimeMs = startTime,
            endTimeMs = now,
            durationMs = durationMs,
            caloriesKcal = calories,
            avgHeartRate = avgHr,
            distanceMeters = distance,
            avgPaceMinPerKm = avgPace,
            sportSpecificSummary = summary
        )

        return dao.insertSession(session)
    }

    private suspend fun checkAndUpdatePR(
        key: String,
        sport: String,
        title: String,
        value: Double,
        displayValue: String,
        isHigherBetter: Boolean,
        achievedAt: Long
    ) {
        val existing = dao.getPRByKey(key)
        val isNewRecord = if (existing == null) {
            true
        } else if (isHigherBetter) {
            value > existing.value
        } else {
            value < existing.value
        }

        if (isNewRecord) {
            dao.insertOrUpdatePR(
                PersonalRecordEntity(
                    recordKey = key,
                    sportType = sport,
                    title = title,
                    value = value,
                    displayValue = displayValue,
                    achievedAtMs = achievedAt
                )
            )
        }
    }

    override fun getAllSessions(): Flow<List<WorkoutSessionEntity>> = dao.getAllSessions()

    override fun getRecentSessions(limit: Int): Flow<List<WorkoutSessionEntity>> = dao.getRecentSessions(limit)

    override fun getAllPRs(): Flow<List<PersonalRecordEntity>> = dao.getAllPRs()

    override suspend fun deleteSession(session: WorkoutSessionEntity) = dao.deleteSession(session)

    override suspend fun clearAll() = dao.clearAllSessions()

    private fun formatPaceString(paceMinPerKm: Double): String {
        if (paceMinPerKm <= 0.1 || paceMinPerKm > 30) return "--'--\""
        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).toInt()
        return String.format("%d'%02d\"", minutes, seconds)
    }
}
