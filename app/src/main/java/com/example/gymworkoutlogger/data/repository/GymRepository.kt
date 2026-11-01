package com.example.gymworkoutlogger.data.repository

import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.PersonalRecord
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    // Exercises
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>
    fun getAllCategories(): Flow<List<String>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exercise: Exercise)

    // Workout Sessions
    fun getAllSessions(): Flow<List<WorkoutSession>>
    fun getSessionWithSets(sessionId: Long): Flow<WorkoutSessionWithSets?>
    fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>>
    suspend fun getActiveSession(): WorkoutSession?
    suspend fun startNewSession(title: String): Long
    suspend fun finishSession(session: WorkoutSession)
    suspend fun deleteSession(session: WorkoutSession)

    // Workout Sets
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>
    suspend fun logSet(set: WorkoutSet): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(set: WorkoutSet)

    // Personal Records
    fun getAllPersonalRecords(): Flow<List<PersonalRecordWithExercise>>
    suspend fun checkAndUpdatePR(exerciseId: Long, weightKg: Double, reps: Int)
}
