package com.sportos.watch.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey
    val recordKey: String, // e.g. "RUN_FASTEST_5K", "BASKETBALL_MAX_VERTICAL", "FOOTBALL_TOP_SPEED", "CRICKET_TOP_ARM_SPEED", "TENNIS_LONGEST_RALLY"
    val sportType: String,
    val title: String,
    val value: Double,
    val displayValue: String,
    val achievedAtMs: Long
)
