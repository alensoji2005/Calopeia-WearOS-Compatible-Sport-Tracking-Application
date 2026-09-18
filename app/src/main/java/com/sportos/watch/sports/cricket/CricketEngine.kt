package com.sportos.watch.sports.cricket

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseUpdate
import com.sportos.watch.core.imu.ImuData
import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.SportEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class CricketMode {
    BOWLING,
    BATTING
}

data class CricketState(
    override val elapsedTimeMs: Long = 0,
    override val activeHeartRate: Double = 0.0,
    override val caloriesKcal: Double = 0.0,
    override val isPaused: Boolean = false,
    override val currentLapNumber: Int = 1, // represents Over or Innings
    val mode: CricketMode = CricketMode.BOWLING,
    val distanceMeters: Double = 0.0,
    val oversBowled: Int = 0,
    val ballsBowledInCurrentOver: Int = 0,
    val totalBallsBowled: Int = 0,
    val maidenOvers: Int = 0,
    val wicketsTaken: Int = 0,
    val runsConceded: Int = 0,
    val lastDeliverySpeedKmh: Double = 0.0,
    val topDeliverySpeedKmh: Double = 0.0,
    val burstSprintsCount: Int = 0, // running between wickets
    val battingBallsFaced: Int = 0,
    val battingRunsScored: Int = 0
) : SportEngineState() {
    val overDisplay: String
        get() = "$oversBowled.$ballsBowledInCurrentOver"
    
    val bowlCount: Int
        get() = totalBallsBowled
}

class CricketEngine(
    val mode: CricketMode = CricketMode.BOWLING
) : SportEngine {

    private val _state = MutableStateFlow(CricketState(mode = mode))
    override val engineState: StateFlow<SportEngineState> = _state.asStateFlow()

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var isActive = false
    private var isPaused = false

    private var isSprinting = false
    private var topArmSpeed = 0.0
    private var runsInCurrentOver = 0

    override fun start() {
        startTimeMs = System.currentTimeMillis()
        isActive = true
        isPaused = false
        pausedDurationMs = 0
    }

    override fun pause() {
        if (isActive && !isPaused) {
            isPaused = true
            pauseStartTimeMs = System.currentTimeMillis()
            _state.value = _state.value.copy(isPaused = true)
        }
    }

    override fun resume() {
        if (isActive && isPaused) {
            pausedDurationMs += System.currentTimeMillis() - pauseStartTimeMs
            isPaused = false
            _state.value = _state.value.copy(isPaused = false)
        }
    }

    override fun processImuData(data: ImuData) {
        if (!isActive || isPaused) return
        
        // Arm swing delivery speed estimation from angular velocity
        if (mode == CricketMode.BOWLING) {
            val gyroMag = sqrt(data.gyroX*data.gyroX + data.gyroY*data.gyroY + data.gyroZ*data.gyroZ)
            // If sharp high angular velocity swing (> 12 rad/s ~ 680 deg/s)
            if (gyroMag > 12.0f) {
                // Estimated release speed (km/h) proportional to wrist/arm angular velocity
                val estimatedSpeed = gyroMag * 8.2 // e.g. 15 rad/s -> ~123 km/h
                topArmSpeed = max(topArmSpeed, estimatedSpeed.toDouble())
            }
        }
    }

    override fun processHealthData(update: ExerciseUpdate) {
        if (!isActive || isPaused) return
        val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: _state.value.activeHeartRate
        val distance = update.latestMetrics.getData(DataType.DISTANCE_TOTAL)?.total ?: _state.value.distanceMeters
        val speedMps = update.latestMetrics.getData(DataType.PACE).lastOrNull()?.value ?: 0.0
        val speedKmh = speedMps * 3.6

        var sprintCount = _state.value.burstSprintsCount
        // Burst sprint (e.g. running between wickets or bowling run-up) > 17km/h
        if (speedKmh > 17.0 && !isSprinting) {
            isSprinting = true
            sprintCount++
        } else if (speedKmh < 10.0) {
            isSprinting = false
        }

        val elapsed = max(0L, System.currentTimeMillis() - startTimeMs - pausedDurationMs)
        val calories = (distance / 1000.0) * 60.0 + (sprintCount * 3.5)

        _state.value = _state.value.copy(
            elapsedTimeMs = elapsed,
            activeHeartRate = hr,
            caloriesKcal = calories,
            isPaused = isPaused,
            distanceMeters = distance,
            burstSprintsCount = sprintCount
        )
    }

    fun logDelivery(speedKmh: Double? = null) {
        val current = _state.value
        var balls = current.ballsBowledInCurrentOver + 1
        var overs = current.oversBowled
        var maidens = current.maidenOvers
        val speed = speedKmh ?: (95.0 + (System.currentTimeMillis() % 35))

        topArmSpeed = max(topArmSpeed, speed)

        if (balls >= 6) {
            balls = 0
            overs++
            if (runsInCurrentOver == 0) {
                maidens++
            }
            runsInCurrentOver = 0
        }

        _state.value = current.copy(
            ballsBowledInCurrentOver = balls,
            oversBowled = overs,
            totalBallsBowled = current.totalBallsBowled + 1,
            maidenOvers = maidens,
            lastDeliverySpeedKmh = ((speed * 10).roundToInt()) / 10.0,
            topDeliverySpeedKmh = ((topArmSpeed * 10).roundToInt()) / 10.0
        )
    }

    fun logWicket() {
        _state.value = _state.value.copy(
            wicketsTaken = _state.value.wicketsTaken + 1
        )
    }

    fun logRuns(runs: Int) {
        runsInCurrentOver += runs
        _state.value = _state.value.copy(
            runsConceded = _state.value.runsConceded + runs
        )
    }

    fun logBattingShot(runs: Int) {
        _state.value = _state.value.copy(
            battingBallsFaced = _state.value.battingBallsFaced + 1,
            battingRunsScored = _state.value.battingRunsScored + runs
        )
    }

    override fun triggerLap() {
        // End of spell or innings break
        _state.value = _state.value.copy(
            currentLapNumber = _state.value.currentLapNumber + 1
        )
    }

    override fun stop() {
        isActive = false
        isPaused = false
    }

    fun updateLiveSimulation(
        elapsedMs: Long,
        heartRate: Double,
        overs: Int,
        balls: Int,
        lastSpeed: Double,
        sprints: Int,
        calories: Double
    ) {
        _state.value = _state.value.copy(
            elapsedTimeMs = elapsedMs,
            activeHeartRate = heartRate,
            oversBowled = overs,
            ballsBowledInCurrentOver = balls,
            lastDeliverySpeedKmh = lastSpeed,
            topDeliverySpeedKmh = max(_state.value.topDeliverySpeedKmh, lastSpeed),
            burstSprintsCount = sprints,
            caloriesKcal = calories
        )
    }
}

