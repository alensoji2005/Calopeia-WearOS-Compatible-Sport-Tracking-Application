package com.sportos.watch.sports

import androidx.health.services.client.data.ExerciseUpdate
import com.sportos.watch.core.imu.ImuData
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for all modular sport engines (Strategy Pattern).
 * Each sport (Running, Basketball, etc.) implements this engine to process
 * IMU data and Health Services data differently based on its biomechanical needs.
 */
interface SportEngine {
    
    /**
     * Called when the sport engine is started.
     */
    fun start()

    /**
     * Process high-rate IMU data (50Hz-100Hz).
     */
    fun processImuData(data: ImuData)

    /**
     * Process low-rate Health Services data (GPS, HR, Steps).
     */
    fun processHealthData(update: ExerciseUpdate)

    /**
     * Pause the workout session.
     */
    fun pause()

    /**
     * Resume the workout session.
     */
    fun resume()

    /**
     * Called to manually trigger a lap or split.
     */
    fun triggerLap()

    /**
     * Called when the sport engine is stopped.
     */
    fun stop()

    /**
     * A unified state flow exposing the current UI metrics (e.g. Pace, HR, Jump Height)
     * so the Compose UI can observe it generically.
     */
    val engineState: StateFlow<SportEngineState>
}

abstract class SportEngineState {
    abstract val elapsedTimeMs: Long
    abstract val activeHeartRate: Double
    abstract val caloriesKcal: Double
    abstract val isPaused: Boolean
    abstract val currentLapNumber: Int
}

