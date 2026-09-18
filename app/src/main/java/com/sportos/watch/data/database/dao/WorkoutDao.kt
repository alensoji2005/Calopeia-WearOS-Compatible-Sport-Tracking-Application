package com.sportos.watch.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportos.watch.data.database.entity.PersonalRecordEntity
import com.sportos.watch.data.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMs DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAllSessions()

    // Personal Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePR(pr: PersonalRecordEntity)

    @Query("SELECT * FROM personal_records ORDER BY achievedAtMs DESC")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE recordKey = :key")
    suspend fun getPRByKey(key: String): PersonalRecordEntity?
}
