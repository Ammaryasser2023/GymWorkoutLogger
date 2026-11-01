package com.example.gymworkoutlogger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: WorkoutSet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<WorkoutSet>)

    @Update
    suspend fun update(set: WorkoutSet)

    @Delete
    suspend fun delete(set: WorkoutSet)

    @Query("SELECT * FROM workout_sets WHERE workout_session_id = :sessionId ORDER BY set_number ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE exercise_id = :exerciseId ORDER BY weight_kg DESC LIMIT 1")
    suspend fun getMaxWeightSetForExercise(exerciseId: Long): WorkoutSet?
}
