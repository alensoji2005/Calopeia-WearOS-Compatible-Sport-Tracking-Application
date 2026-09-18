package com.sportos.watch.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sportType: String,
    val subMode: String = "",
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val caloriesKcal: Double,
    val avgHeartRate: Double,
    val maxHeartRate: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val avgPaceMinPerKm: Double = 0.0,
    val sportSpecificSummary: String = "",
    val serializedRoutePoints: String = "", // Optional JSON or coordinate breadcrumbs
    val isSynced: Boolean = false
)
