package com.example.gymworkoutlogger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSession): Long

    @Update
    suspend fun update(session: WorkoutSession)

    @Delete
    suspend fun delete(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithSets(sessionId: Long): Flow<WorkoutSessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>>

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSession?
}
