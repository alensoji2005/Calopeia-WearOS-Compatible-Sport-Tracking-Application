package com.sportos.watch.sports.running

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseUpdate
import com.sportos.watch.core.imu.ImuData
import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.SportEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

enum class RunningMode {
    FREE_RUN,
    GHOST_PACER
}

data class GpsLocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val timestampMs: Long = 0L,
    val speedMps: Float = 0f,
    val bearingDegrees: Float = 0f
)

data class LapSplit(
    val lapNumber: Int,
    val splitTimeMs: Long,
    val distanceMeters: Double,
    val paceMinPerKm: Double,
    val elevationGainMeters: Double = 0.0,
    val avgHeartRate: Double = 158.0
)

data class RunningState(
    override val elapsedTimeMs: Long = 0,
    override val activeHeartRate: Double = 0.0,
    override val caloriesKcal: Double = 0.0,
    override val isPaused: Boolean = false,
    override val currentLapNumber: Int = 1,
    val mode: RunningMode = RunningMode.GHOST_PACER,
    val distanceMeters: Double = 0.0,
    val currentPaceMinPerKm: Double = 0.0,
    val averagePaceMinPerKm: Double = 0.0,
    val cadenceSpm: Double = 0.0,
    val targetPaceMinPerKm: Double = 5.0, // 5:00 min/km default target
    val targetPaceDeltaMs: Long = 0, // Ghost Runner Delta (+ ahead, - behind)
    val targetDistanceDeltaMeters: Double = 0.0,
    val ghostProgress: Float = 0.0f,
    val currentLapDistanceMeters: Double = 0.0,
    val currentLapTimeMs: Long = 0,
    val laps: List<LapSplit> = emptyList(),
    val routePoints: List<GpsLocationPoint> = emptyList(),
    val currentElevationMeters: Double = 45.0,
    val elevationGainMeters: Double = 0.0,
    val activeSegmentName: String = "Lake Loop Sprint",
    val segmentPrDeltaMs: Long = -3000L, // -3s ahead of PR
    val segmentDistanceRemainingM: Double = 420.0,
    val segmentTargetPaceMinPerKm: Double = 4.75, // 4:45 PR
    val kmSplits: List<LapSplit> = emptyList(),
    val isAutoPaused: Boolean = false
) : SportEngineState()

class RunningEngine(
    private val targetPaceMinPerKm: Double = 5.0,
    private val mode: RunningMode = RunningMode.GHOST_PACER
) : SportEngine {

    private val _state = MutableStateFlow(
        RunningState(
            targetPaceMinPerKm = targetPaceMinPerKm,
            mode = mode
        )
    )
    override val engineState: StateFlow<SportEngineState> = _state.asStateFlow()

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var isRunning = false
    private var isPaused = false

    private var lapStartDistance = 0.0
    private var lapStartTimeMs = 0L

    // Internal mutable buffer for route points to eliminate ~6.5M array allocations over a long run
    private val mutableRoutePoints = ArrayList<GpsLocationPoint>(500)
    // Pre-allocated array for Location.distanceBetween calculation
    private val distanceCalculationResult = FloatArray(1)

    override fun start() {
        startTimeMs = System.currentTimeMillis()
        lapStartTimeMs = startTimeMs
        isRunning = true
        isPaused = false
        pausedDurationMs = 0
        mutableRoutePoints.clear()
    }

    override fun pause() {
        if (isRunning && !isPaused) {
            isPaused = true
            pauseStartTimeMs = System.currentTimeMillis()
            _state.value = _state.value.copy(isPaused = true)
        }
    }

    override fun resume() {
        if (isRunning && isPaused) {
            pausedDurationMs += System.currentTimeMillis() - pauseStartTimeMs
            isPaused = false
            _state.value = _state.value.copy(isPaused = false)
        }
    }

    override fun processImuData(data: ImuData) {
        // High-frequency IMU processing can be used for step/cadence refinement
    }

    override fun processHealthData(update: ExerciseUpdate) {
        if (!isRunning || isPaused) return
        
        val hr = update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: _state.value.activeHeartRate
        val distance = update.latestMetrics.getData(DataType.DISTANCE_TOTAL)?.total ?: _state.value.distanceMeters
        val speedMps = update.latestMetrics.getData(DataType.PACE).lastOrNull()?.value ?: 0.0
        val cadence = update.latestMetrics.getData(DataType.STEPS_PER_MINUTE).lastOrNull()?.value?.toDouble() ?: _state.value.cadenceSpm

        val now = System.currentTimeMillis()
        val elapsedTime = max(0L, now - startTimeMs - pausedDurationMs)
        
        // Target speed in meters per second
        // Target pace: min/km -> seconds per km = targetPace * 60
        // speed (m/s) = 1000 / (targetPace * 60)
        val targetSpeedMpS = if (targetPaceMinPerKm > 0) 1000.0 / (targetPaceMinPerKm * 60.0) else 3.33
        val expectedDistance = (elapsedTime / 1000.0) * targetSpeedMpS
        val distanceDelta = distance - expectedDistance
        val deltaMs = (distanceDelta / targetSpeedMpS * 1000.0).toLong()

        // Calories estimate: roughly 1 kcal per kg per km, assuming 70kg runner ~ 70 kcal/km
        val calories = (distance / 1000.0) * 70.0

        val currentPace = if (speedMps > 0.5) 1000.0 / (speedMps * 60.0) else 0.0
        val avgPace = if (distance > 50 && elapsedTime > 5000) {
            (elapsedTime / 1000.0 / 60.0) / (distance / 1000.0)
        } else {
            currentPace
        }

        val lapDistance = max(0.0, distance - lapStartDistance)
        val lapTime = max(0L, now - lapStartTimeMs)

        // Ghost progress relative to 5km or 10km loop
        val ghostProgress = ((distance % 5000.0) / 5000.0).toFloat()

        _state.value = _state.value.copy(
            elapsedTimeMs = elapsedTime,
            activeHeartRate = hr,
            caloriesKcal = calories,
            isPaused = isPaused,
            distanceMeters = distance,
            currentPaceMinPerKm = currentPace,
            averagePaceMinPerKm = avgPace,
            cadenceSpm = cadence,
            targetPaceDeltaMs = deltaMs,
            targetDistanceDeltaMeters = distanceDelta,
            ghostProgress = ghostProgress,
            currentLapDistanceMeters = lapDistance,
            currentLapTimeMs = lapTime
        )
    }

    override fun triggerLap() {
        val current = _state.value
        val lapNum = current.currentLapNumber
        val lapDist = current.currentLapDistanceMeters
        val lapTime = current.currentLapTimeMs
        val lapPace = if (lapDist > 10) (lapTime / 1000.0 / 60.0) / (lapDist / 1000.0) else current.currentPaceMinPerKm

        val newLap = LapSplit(
            lapNumber = lapNum,
            splitTimeMs = lapTime,
            distanceMeters = lapDist,
            paceMinPerKm = lapPace
        )

        lapStartDistance = current.distanceMeters
        lapStartTimeMs = System.currentTimeMillis()

        _state.value = current.copy(
            currentLapNumber = lapNum + 1,
            currentLapDistanceMeters = 0.0,
            currentLapTimeMs = 0,
            laps = current.laps + newLap
        )
    }

    override fun stop() {
        isRunning = false
        isPaused = false
    }

    private fun isSignificantPoint(last: GpsLocationPoint?, candidate: GpsLocationPoint): Boolean {
        if (last == null) return true
        android.location.Location.distanceBetween(
            last.latitude, last.longitude,
            candidate.latitude, candidate.longitude,
            distanceCalculationResult
        )
        val dist = distanceCalculationResult[0]
        if (dist >= 3.0f) return true
        if (dist >= 1.5f) {
            val bearingDiff = kotlin.math.abs(candidate.bearingDegrees - last.bearingDegrees)
            val normalizedDiff = if (bearingDiff > 180f) 360f - bearingDiff else bearingDiff
            if (normalizedDiff > 20f) return true
        }
        return false
    }

    fun addGpsPoint(point: GpsLocationPoint) {
        val current = _state.value
        val lastPoint = mutableRoutePoints.lastOrNull()
        val isNewPoint = isSignificantPoint(lastPoint, point)
        
        if (isNewPoint) {
            mutableRoutePoints.add(point)
        }

        val prevEle = current.currentElevationMeters
        val eleDiff = point.altitudeMeters - prevEle
        val newGain = if (eleDiff > 0) current.elevationGainMeters + eleDiff else current.elevationGainMeters

        _state.value = current.copy(
            routePoints = if (isNewPoint) ArrayList(mutableRoutePoints) else current.routePoints,
            currentElevationMeters = point.altitudeMeters,
            elevationGainMeters = newGain
        )
    }

    // Direct update helper (used by simulation or test harness)
    fun updateLiveMetrics(
        elapsedMs: Long,
        heartRate: Double,
        distanceM: Double,
        paceMinKm: Double,
        cadence: Double,
        deltaMs: Long,
        deltaDistM: Double,
        calories: Double,
        elevationGainM: Double = 0.0,
        gpsPoint: GpsLocationPoint? = null
    ) {
        val current = _state.value
        val isNewPoint = if (gpsPoint != null) {
            val lastPoint = mutableRoutePoints.lastOrNull()
            if (isSignificantPoint(lastPoint, gpsPoint)) {
                mutableRoutePoints.add(gpsPoint)
                true
            } else false
        } else false

        val updatedPoints = if (isNewPoint) ArrayList(mutableRoutePoints) else current.routePoints

        // Calculate dynamic km splits - only allocate when a new km milestone is reached
        val kmCompleted = (distanceM / 1000.0).toInt()
        val updatedSplits = if (kmCompleted > current.kmSplits.size && kmCompleted > 0) {
            val currentSplits = current.kmSplits.toMutableList()
            val splitTime = elapsedMs / kmCompleted
            val splitPace = (splitTime / 1000.0 / 60.0)
            currentSplits.add(
                LapSplit(
                    lapNumber = kmCompleted,
                    splitTimeMs = splitTime,
                    distanceMeters = 1000.0,
                    paceMinPerKm = splitPace,
                    elevationGainMeters = elevationGainM / kmCompleted,
                    avgHeartRate = heartRate
                )
            )
            currentSplits
        } else {
            current.kmSplits
        }

        // Live segment calculation
        val segmentDistRemaining = max(0.0, 500.0 - (distanceM % 500.0))
        val prDelta = (deltaMs * 0.8).toLong()

        _state.value = current.copy(
            elapsedTimeMs = elapsedMs,
            activeHeartRate = heartRate,
            distanceMeters = distanceM,
            currentPaceMinPerKm = paceMinKm,
            cadenceSpm = cadence,
            targetPaceDeltaMs = deltaMs,
            targetDistanceDeltaMeters = deltaDistM,
            caloriesKcal = calories,
            routePoints = updatedPoints,
            elevationGainMeters = elevationGainM,
            kmSplits = updatedSplits,
            segmentDistanceRemainingM = segmentDistRemaining,
            segmentPrDeltaMs = prDelta
        )
    }
}

