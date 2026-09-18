package com.sportos.watch.sports.basketball

import androidx.compose.runtime.Immutable
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseUpdate
import com.sportos.watch.core.imu.ImuData
import com.sportos.watch.core.imu.ImuRingBuffer
import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.SportEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class BasketballMode {
    GAME_AUTOMATIC,
    PRACTICE_DRILL
}

@Immutable
data class BasketballState(
    override val elapsedTimeMs: Long = 0,
    override val activeHeartRate: Double = 0.0,
    override val caloriesKcal: Double = 0.0,
    override val isPaused: Boolean = false,
    override val currentLapNumber: Int = 1, // represents Quarter
    val mode: BasketballMode = BasketballMode.GAME_AUTOMATIC,
    val quarter: Int = 1,
    val shotsAttempted: Int = 0,
    val shotsMade: Int = 0,
    val shotsMissed: Int = 0,
    val shootingPercentage: Double = 0.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val jumpCount: Int = 0,
    val lastJumpHeightInches: Double = 0.0,
    val maxJumpHeightInches: Double = 0.0,
    val avgJumpHeightInches: Double = 0.0,
    val lastHangTimeMs: Long = 0,
    val playerLoad: Double = 0.0
) : SportEngineState()

class BasketballEngine(
    val mode: BasketballMode = BasketballMode.GAME_AUTOMATIC
) : SportEngine {

    private val _state = MutableStateFlow(BasketballState(mode = mode))
    override val engineState: StateFlow<SportEngineState> = _state.asStateFlow()

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var isActive = false
    private var isPaused = false

    private val imuRingBuffer = ImuRingBuffer(capacity = 100) // 2 seconds of data at 50Hz
    private var lastImu: ImuData? = null
    private var accumulatedPlayerLoad = 0.0

    // Kinematic jump state machine
    private var isFreeFalling = false
    private var freeFallStartTimeMs: Long = 0
    private var totalJumpHeightsSum = 0.0

    // Shot detection debounce
    private var lastShotDetectedTimeMs: Long = 0
    private var lastUiUpdateTimeMs: Long = 0

    override fun start() {
        startTimeMs = System.currentTimeMillis()
        isActive = true
        isPaused = false
        pausedDurationMs = 0
        imuRingBuffer.clear()
        accumulatedPlayerLoad = 0.0
        lastUiUpdateTimeMs = 0
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
        
        imuRingBuffer.add(data)
        
        // 1. Catapult-style PlayerLoad calculation
        lastImu?.let { prev ->
            val dX = (data.accX - prev.accX).toDouble()
            val dY = (data.accY - prev.accY).toDouble()
            val dZ = (data.accZ - prev.accZ).toDouble()
            accumulatedPlayerLoad += sqrt(dX*dX + dY*dY + dZ*dZ) / 100.0
        }
        lastImu = data

        // 2. Kinematic Jump Detection via Freefall Hang-time
        val normAcc = sqrt(data.accX*data.accX + data.accY*data.accY + data.accZ*data.accZ)
        val now = System.currentTimeMillis()

        if (!isFreeFalling && normAcc < 3.2f) { // Freefall phase (near 0G)
            isFreeFalling = true
            freeFallStartTimeMs = now
        } else if (isFreeFalling && normAcc > 18.0f) { // Landing impact
            isFreeFalling = false
            val hangTimeMs = now - freeFallStartTimeMs
            // Realistic basketball jump: hangtime between 280ms and 850ms
            if (hangTimeMs in 280..850) {
                val hangTimeSec = hangTimeMs / 1000.0
                // h = 0.5 * g * (t/2)^2 = 1/8 * 9.81 * t^2
                val heightMeters = 0.125 * 9.81 * (hangTimeSec * hangTimeSec)
                val heightInches = heightMeters * 39.3701
                recordJump(heightInches, hangTimeMs)
            }
        }

        // 3. Automatic Shot Detection in Game Mode
        if (mode == BasketballMode.GAME_AUTOMATIC) {
            val gyroMagnitude = sqrt(data.gyroX*data.gyroX + data.gyroY*data.gyroY + data.gyroZ*data.gyroZ)
            // Characteristic wrist release snap > 7.5 rad/s (~430 deg/s) with debounce
            if (gyroMagnitude > 7.5f && (now - lastShotDetectedTimeMs > 2500)) {
                lastShotDetectedTimeMs = now
                registerAutoShot()
            }
        }

        val elapsed = max(0L, now - startTimeMs - pausedDurationMs)
        // Throttle continuous background state updates to 1Hz to eliminate 50Hz Compose recomposition churn
        if (now - lastUiUpdateTimeMs >= 1000L) {
            lastUiUpdateTimeMs = now
            val calories = accumulatedPlayerLoad * 1.6 + (elapsed / 1000.0 / 60.0) * 8.5
            _state.value = _state.value.copy(
                elapsedTimeMs = elapsed,
                playerLoad = ((accumulatedPlayerLoad * 10).roundToInt()) / 10.0,
                caloriesKcal = calories,
                isPaused = isPaused
            )
        }
    }

    override fun processHealthData(update: ExerciseUpdate) {
        if (!isActive || isPaused) return
        val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: _state.value.activeHeartRate
        val elapsed = max(0L, System.currentTimeMillis() - startTimeMs - pausedDurationMs)

        _state.value = _state.value.copy(
            activeHeartRate = hr,
            elapsedTimeMs = elapsed
        )
    }

    fun recordJump(heightInches: Double, hangTimeMs: Long) {
        val current = _state.value
        val newCount = current.jumpCount + 1
        totalJumpHeightsSum += heightInches
        val newMax = max(current.maxJumpHeightInches, heightInches)
        val newAvg = totalJumpHeightsSum / newCount

        _state.value = current.copy(
            jumpCount = newCount,
            lastJumpHeightInches = ((heightInches * 10).roundToInt()) / 10.0,
            maxJumpHeightInches = ((newMax * 10).roundToInt()) / 10.0,
            avgJumpHeightInches = ((newAvg * 10).roundToInt()) / 10.0,
            lastHangTimeMs = hangTimeMs
        )
    }

    private fun registerAutoShot() {
        val current = _state.value
        val newAttempted = current.shotsAttempted + 1
        val pct = if (newAttempted > 0) (current.shotsMade.toDouble() / newAttempted) * 100.0 else 0.0
        _state.value = current.copy(
            shotsAttempted = newAttempted,
            shootingPercentage = ((pct * 10).roundToInt()) / 10.0
        )
    }

    // Practice Drill: Log Made Shot
    fun logMadeShot() {
        val current = _state.value
        val attempted = current.shotsAttempted + 1
        val made = current.shotsMade + 1
        val streak = current.currentStreak + 1
        val bestStreak = max(current.bestStreak, streak)
        val pct = (made.toDouble() / attempted) * 100.0

        _state.value = current.copy(
            shotsAttempted = attempted,
            shotsMade = made,
            currentStreak = streak,
            bestStreak = bestStreak,
            shootingPercentage = ((pct * 10).roundToInt()) / 10.0
        )
    }

    // Practice Drill: Log Missed Shot
    fun logMissedShot() {
        val current = _state.value
        val attempted = current.shotsAttempted + 1
        val missed = current.shotsMissed + 1
        val pct = (current.shotsMade.toDouble() / attempted) * 100.0

        _state.value = current.copy(
            shotsAttempted = attempted,
            shotsMissed = missed,
            currentStreak = 0,
            shootingPercentage = ((pct * 10).roundToInt()) / 10.0
        )
    }

    fun undoLastShot() {
        val current = _state.value
        if (current.shotsAttempted > 0) {
            val attempted = current.shotsAttempted - 1
            val made = max(0, current.shotsMade - 1)
            val pct = if (attempted > 0) (made.toDouble() / attempted) * 100.0 else 0.0
            _state.value = current.copy(
                shotsAttempted = attempted,
                shotsMade = made,
                shootingPercentage = ((pct * 10).roundToInt()) / 10.0
            )
        }
    }

    override fun triggerLap() {
        // Quarter Advance: Q1 -> Q2 -> Q3 -> Q4 -> OT
        val nextQ = _state.value.quarter + 1
        _state.value = _state.value.copy(
            quarter = nextQ,
            currentLapNumber = nextQ
        )
    }

    override fun stop() {
        isActive = false
        isPaused = false
    }

    // Direct helper for simulator/tests
    fun updateLiveSimulation(
        elapsedMs: Long,
        heartRate: Double,
        jumps: Int,
        lastHeight: Double,
        maxHeight: Double,
        pLoad: Double,
        calories: Double
    ) {
        _state.value = _state.value.copy(
            elapsedTimeMs = elapsedMs,
            activeHeartRate = heartRate,
            jumpCount = jumps,
            lastJumpHeightInches = lastHeight,
            maxJumpHeightInches = maxHeight,
            playerLoad = pLoad,
            caloriesKcal = calories
        )
    }
}

