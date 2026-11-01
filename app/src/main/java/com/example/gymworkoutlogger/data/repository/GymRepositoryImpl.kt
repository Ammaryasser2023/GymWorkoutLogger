package com.example.gymworkoutlogger.data.repository

import com.example.gymworkoutlogger.data.dao.ExerciseDao
import com.example.gymworkoutlogger.data.dao.PersonalRecordDao
import com.example.gymworkoutlogger.data.dao.WorkoutSessionDao
import com.example.gymworkoutlogger.data.dao.WorkoutSetDao
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.PersonalRecord
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GymRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: WorkoutSessionDao,
    private val setDao: WorkoutSetDao,
    private val prDao: PersonalRecordDao
) : GymRepository {

    override fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    override fun getExercisesByCategory(category: String): Flow<List<Exercise>> =
        exerciseDao.getExercisesByCategory(category)

    override fun getAllCategories(): Flow<List<String>> = exerciseDao.getAllCategories()

    override suspend fun getExerciseById(id: Long): Exercise? = withContext(Dispatchers.IO) {
        exerciseDao.getExerciseById(id)
    }

    override suspend fun insertExercise(exercise: Exercise): Long = withContext(Dispatchers.IO) {
        exerciseDao.insert(exercise)
    }

    override suspend fun updateExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        exerciseDao.update(exercise)
    }

    override suspend fun deleteExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        exerciseDao.delete(exercise)
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> = sessionDao.getAllSessions()

    override fun getSessionWithSets(sessionId: Long): Flow<WorkoutSessionWithSets?> =
        sessionDao.getSessionWithSets(sessionId)

    override fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>> =
        sessionDao.getAllSessionsWithSets()

    override suspend fun getActiveSession(): WorkoutSession? = withContext(Dispatchers.IO) {
        sessionDao.getActiveSession()
    }

    override suspend fun startNewSession(title: String): Long = withContext(Dispatchers.IO) {
        val session = WorkoutSession(
            title = title,
            startTime = System.currentTimeMillis()
        )
        sessionDao.insert(session)
    }

    override suspend fun finishSession(session: WorkoutSession) = withContext(Dispatchers.IO) {
        val completedSession = session.copy(endTime = System.currentTimeMillis())
        sessionDao.update(completedSession)
    }

    override suspend fun deleteSession(session: WorkoutSession) = withContext(Dispatchers.IO) {
        sessionDao.delete(session)
    }

    override fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>> =
        setDao.getSetsForSession(sessionId)

    override suspend fun logSet(set: WorkoutSet): Long = withContext(Dispatchers.IO) {
        val id = setDao.insert(set)
        if (set.isCompleted) {
            checkAndUpdatePR(set.exerciseId, set.weightKg, set.reps)
        }
        id
    }

    override suspend fun updateSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        setDao.update(set)
        if (set.isCompleted) {
            checkAndUpdatePR(set.exerciseId, set.weightKg, set.reps)
        }
    }

    override suspend fun deleteSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        setDao.delete(set)
    }

    override fun getAllPersonalRecords(): Flow<List<PersonalRecordWithExercise>> =
        prDao.getAllPersonalRecordsWithExercise()

    override suspend fun checkAndUpdatePR(exerciseId: Long, weightKg: Double, reps: Int) =
        withContext(Dispatchers.IO) {
            val currentPR = prDao.getPrForExercise(exerciseId)
            if (currentPR == null || weightKg > currentPR.maxWeightKg) {
                val updatedPR = PersonalRecord(
                    id = currentPR?.id ?: 0,
                    exerciseId = exerciseId,
                    maxWeightKg = weightKg,
                    maxReps = reps,
                    achievedTimestamp = System.currentTimeMillis()
                )
                prDao.insertOrUpdate(updatedPR)
            }
        }
}
