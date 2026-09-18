package com.sportos.watch.core.healthservices

import android.content.Context
import android.util.Log
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await

class HealthServicesManager(context: Context) {
    
    private val exerciseClient: ExerciseClient = HealthServices.getClient(context).exerciseClient
    private val _exerciseState = MutableStateFlow<ExerciseUpdate?>(null)
    val exerciseState: StateFlow<ExerciseUpdate?> = _exerciseState.asStateFlow()

    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            _exerciseState.value = update
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            // Handle lap summary natively from Health Services
        }

        override fun onRegistered() {
            Log.d(TAG, "ExerciseUpdateCallback registered")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e(TAG, "ExerciseUpdateCallback registration failed", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            // e.g. GPS acquired, HR sensor locked
        }
    }

    suspend fun prepareExercise(exerciseType: ExerciseType) {
        val warmUpConfig = WarmUpConfig(
            exerciseType = exerciseType,
            dataTypes = setOf(
                DataType.HEART_RATE_BPM,
                DataType.LOCATION
            )
        )
        try {
            exerciseClient.prepareExerciseAsync(warmUpConfig).await()
            Log.d(TAG, "Prepared exercise: $exerciseType")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare exercise", e)
        }
    }

    suspend fun startExercise(exerciseType: ExerciseType, dataTypes: Set<DataType<*, *>>) {
        val config = ExerciseConfig.builder(exerciseType)
            .setDataTypes(dataTypes)
            .setIsAutoPauseAndResumeEnabled(true)
            .build()
            
        try {
            exerciseClient.setUpdateCallback(exerciseUpdateCallback)
            exerciseClient.startExerciseAsync(config).await()
            Log.d(TAG, "Started exercise: $exerciseType")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start exercise", e)
        }
    }

    suspend fun pauseExercise() {
        try {
            exerciseClient.pauseExerciseAsync().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause exercise", e)
        }
    }

    suspend fun resumeExercise() {
        try {
            exerciseClient.resumeExerciseAsync().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume exercise", e)
        }
    }

    suspend fun endExercise() {
        try {
            exerciseClient.endExerciseAsync().await()
            exerciseClient.clearUpdateCallbackAsync(exerciseUpdateCallback).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end exercise", e)
        }
    }

    companion object {
        private const val TAG = "HealthServicesManager"
    }
}
