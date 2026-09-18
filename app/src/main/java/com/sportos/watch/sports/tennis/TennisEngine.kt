package com.sportos.watch.sports.tennis

import androidx.compose.runtime.Immutable
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

enum class StrokeType {
    FOREHAND,
    BACKHAND,
    SERVE
}

@Immutable
data class TennisState(
    override val elapsedTimeMs: Long = 0,
    override val activeHeartRate: Double = 0.0,
    override val caloriesKcal: Double = 0.0,
    override val isPaused: Boolean = false,
    override val currentLapNumber: Int = 1, // represents Set or Game
    val distanceMeters: Double = 0.0,
    val forehands: Int = 0,
    val backhands: Int = 0,
    val serves: Int = 0,
    val lastRacketSpeedKmh: Double = 0.0,
    val maxRacketSpeedKmh: Double = 0.0,
    val isRallyActive: Boolean = false,
    val activeRallyDurationMs: Long = 0,
    val longestRallyDurationMs: Long = 0,
    val totalRalliesCount: Int = 0
) : SportEngineState() {
    val swingCount: Int
        get() = forehands + backhands + serves
}

class TennisEngine : SportEngine {

    private val _state = MutableStateFlow(TennisState())
    override val engineState: StateFlow<SportEngineState> = _state.asStateFlow()

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var isActive = false
    private var isPaused = false

    private var isRallyActive = false
    private var rallyStartTimeMs: Long = 0
    private var maxRecordedSpeed = 0.0
    private var longestRally = 0L
    private var lastStrokeTimeMs: Long = 0L

    override fun start() {
        startTimeMs = System.currentTimeMillis()
        isActive = true
        isPaused = false
        pausedDurationMs = 0
        lastStrokeTimeMs = 0L
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

        // Classify strokes based on angular velocity & roll
        val gyroMag = sqrt(data.gyroX*data.gyroX + data.gyroY*data.gyroY + data.gyroZ*data.gyroZ)
        val now = System.currentTimeMillis()
        // If high angular velocity racket swing (> 10 rad/s) and debounced to prevent multi-hit frames
        if (gyroMag > 10.0f && (now - lastStrokeTimeMs > 600L)) {
            lastStrokeTimeMs = now
            val speedKmh = gyroMag * 7.5 // approximate racket head speed
            maxRecordedSpeed = max(maxRecordedSpeed, speedKmh.toDouble())

            // Classification by axis orientation:
            // High positive AccZ + high GyroX -> Overhead Serve
            // Positive GyroZ -> Forehand
            // Negative GyroZ -> Backhand
            val type = when {
                data.accZ > 8.0f && data.gyroX > 6.0f -> StrokeType.SERVE
                data.gyroZ > 0 -> StrokeType.FOREHAND
                else -> StrokeType.BACKHAND
            }
            logStroke(type, speedKmh.toDouble())
        }
    }

    fun logStroke(type: StrokeType, speedKmh: Double) {
        val current = _state.value
        val speed = ((speedKmh * 10).roundToInt()) / 10.0
        maxRecordedSpeed = max(maxRecordedSpeed, speed)

        val updated = when (type) {
            StrokeType.FOREHAND -> current.copy(
                forehands = current.forehands + 1,
                lastRacketSpeedKmh = speed,
                maxRacketSpeedKmh = maxRecordedSpeed
            )
            StrokeType.BACKHAND -> current.copy(
                backhands = current.backhands + 1,
                lastRacketSpeedKmh = speed,
                maxRacketSpeedKmh = maxRecordedSpeed
            )
            StrokeType.SERVE -> current.copy(
                serves = current.serves + 1,
                lastRacketSpeedKmh = speed,
                maxRacketSpeedKmh = maxRecordedSpeed
            )
        }
        _state.value = updated
    }

    override fun processHealthData(update: ExerciseUpdate) {
        if (!isActive || isPaused) return
        val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: _state.value.activeHeartRate
        val distance = update.latestMetrics.getData(DataType.DISTANCE_TOTAL)?.total ?: _state.value.distanceMeters

        val now = System.currentTimeMillis()
        val rallyDuration = if (isRallyActive) max(0L, now - rallyStartTimeMs) else _state.value.activeRallyDurationMs
        val elapsed = max(0L, now - startTimeMs - pausedDurationMs)
        val calories = (distance / 1000.0) * 55.0 + (_state.value.swingCount * 0.8)

        _state.value = _state.value.copy(
            elapsedTimeMs = elapsed,
            activeHeartRate = hr,
            caloriesKcal = calories,
            isPaused = isPaused,
            distanceMeters = distance,
            activeRallyDurationMs = rallyDuration
        )
    }

    fun startRally() {
        isRallyActive = true
        rallyStartTimeMs = System.currentTimeMillis()
        _state.value = _state.value.copy(isRallyActive = true)
    }

    fun endRally() {
        if (isRallyActive) {
            val duration = max(0L, System.currentTimeMillis() - rallyStartTimeMs)
            longestRally = max(longestRally, duration)
            isRallyActive = false
            _state.value = _state.value.copy(
                isRallyActive = false,
                activeRallyDurationMs = 0,
                longestRallyDurationMs = longestRally,
                totalRalliesCount = _state.value.totalRalliesCount + 1
            )
        }
    }

    override fun triggerLap() {
        // End of set or game break
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
        distanceM: Double,
        fore: Int,
        back: Int,
        serv: Int,
        racketSpeed: Double,
        rallyMs: Long,
        calories: Double
    ) {
        _state.value = _state.value.copy(
            elapsedTimeMs = elapsedMs,
            activeHeartRate = heartRate,
            distanceMeters = distanceM,
            forehands = fore,
            backhands = back,
            serves = serv,
            lastRacketSpeedKmh = racketSpeed,
            maxRacketSpeedKmh = max(_state.value.maxRacketSpeedKmh, racketSpeed),
            activeRallyDurationMs = rallyMs,
            caloriesKcal = calories
        )
    }
}

