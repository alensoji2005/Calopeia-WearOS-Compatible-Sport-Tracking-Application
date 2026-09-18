package com.sportos.watch.sports.football

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

enum class FootballPeriod(val label: String) {
    FIRST_HALF("1st Half"),
    HALF_TIME("Half Time"),
    SECOND_HALF("2nd Half"),
    EXTRA_TIME("Extra Time"),
    MATCH_OVER("Full Time")
}

data class FootballState(
    override val elapsedTimeMs: Long = 0,
    override val activeHeartRate: Double = 0.0,
    override val caloriesKcal: Double = 0.0,
    override val isPaused: Boolean = false,
    override val currentLapNumber: Int = 1,
    val period: FootballPeriod = FootballPeriod.FIRST_HALF,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val topSpeedKmh: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val sprintCount: Int = 0,
    val highIntensityDistanceMeters: Double = 0.0,
    val workRateIntensityIndex: Double = 0.0
) : SportEngineState()

class FootballEngine : SportEngine {

    private val _state = MutableStateFlow(FootballState())
    override val engineState: StateFlow<SportEngineState> = _state.asStateFlow()

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var isActive = false
    private var isPaused = false
    
    private var lastDistance = 0.0
    private var hird = 0.0 // High Intensity Running Distance (> 15 km/h)
    private var isSprinting = false
    private var maxRecordedSpeed = 0.0

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
        // High rate acceleration used for sudden direction changes / cuts
    }

    override fun processHealthData(update: ExerciseUpdate) {
        if (!isActive || isPaused) return

        val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: _state.value.activeHeartRate
        val distance = update.latestMetrics.getData(DataType.DISTANCE_TOTAL)?.total ?: _state.value.distanceMeters
        val speedMps = update.latestMetrics.getData(DataType.PACE).lastOrNull()?.value ?: 0.0
        val speedKmh = speedMps * 3.6
        
        val deltaDistance = max(0.0, distance - lastDistance)
        lastDistance = distance

        // HIRD threshold > 15 km/h
        if (speedKmh > 15.0) {
            hird += deltaDistance
        }

        // Sprint detection > 20 km/h with hysteresis
        var newSprintCount = _state.value.sprintCount
        if (speedKmh > 20.0 && !isSprinting) {
            isSprinting = true
            newSprintCount++
        } else if (speedKmh < 16.0) {
            isSprinting = false
        }

        maxRecordedSpeed = max(maxRecordedSpeed, speedKmh)

        val elapsed = max(0L, System.currentTimeMillis() - startTimeMs - pausedDurationMs)
        val avgSpeed = if (elapsed > 5000) (distance / (elapsed / 1000.0)) * 3.6 else 0.0
        val calories = (distance / 1000.0) * 65.0 + (newSprintCount * 4.0)
        val workRate = if (distance > 0) (hird / distance) * 100.0 else 0.0

        _state.value = _state.value.copy(
            elapsedTimeMs = elapsed,
            activeHeartRate = hr,
            caloriesKcal = calories,
            isPaused = isPaused,
            distanceMeters = distance,
            currentSpeedKmh = ((speedKmh * 10).roundToInt()) / 10.0,
            topSpeedKmh = ((maxRecordedSpeed * 10).roundToInt()) / 10.0,
            averageSpeedKmh = ((avgSpeed * 10).roundToInt()) / 10.0,
            sprintCount = newSprintCount,
            highIntensityDistanceMeters = ((hird * 10).roundToInt()) / 10.0,
            workRateIntensityIndex = ((workRate * 10).roundToInt()) / 10.0
        )
    }

    override fun triggerLap() {
        advancePeriod()
    }

    fun advancePeriod() {
        val nextPeriod = when (_state.value.period) {
            FootballPeriod.FIRST_HALF -> FootballPeriod.HALF_TIME
            FootballPeriod.HALF_TIME -> FootballPeriod.SECOND_HALF
            FootballPeriod.SECOND_HALF -> FootballPeriod.EXTRA_TIME
            FootballPeriod.EXTRA_TIME -> FootballPeriod.MATCH_OVER
            FootballPeriod.MATCH_OVER -> FootballPeriod.FIRST_HALF
        }
        _state.value = _state.value.copy(
            period = nextPeriod,
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
        speedKmh: Double,
        topSpeed: Double,
        sprints: Int,
        hirdM: Double,
        calories: Double
    ) {
        _state.value = _state.value.copy(
            elapsedTimeMs = elapsedMs,
            activeHeartRate = heartRate,
            distanceMeters = distanceM,
            currentSpeedKmh = speedKmh,
            topSpeedKmh = topSpeed,
            sprintCount = sprints,
            highIntensityDistanceMeters = hirdM,
            caloriesKcal = calories
        )
    }
}

