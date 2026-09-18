package com.sportos.watch.domain.repository

import com.sportos.watch.data.database.entity.PersonalRecordEntity
import com.sportos.watch.data.database.entity.WorkoutSessionEntity
import com.sportos.watch.sports.SportEngineState
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun saveWorkoutSession(sportType: String, subMode: String, state: SportEngineState?): Long
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>
    fun getRecentSessions(limit: Int): Flow<List<WorkoutSessionEntity>>
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>
    suspend fun deleteSession(session: WorkoutSessionEntity)
    suspend fun clearAll()
}
