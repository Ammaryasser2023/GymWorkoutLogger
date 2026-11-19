// Member 1: Main Room Database Setup & Pre-population
package com.example.gymworkoutlogger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymworkoutlogger.data.converter.DateConverter
import com.example.gymworkoutlogger.data.dao.ExerciseDao
import com.example.gymworkoutlogger.data.dao.PersonalRecordDao
import com.example.gymworkoutlogger.data.dao.WorkoutSessionDao
import com.example.gymworkoutlogger.data.dao.WorkoutSetDao
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.PersonalRecord
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Exercise::class,
        WorkoutSession::class,
        WorkoutSet::class,
        PersonalRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun personalRecordDao(): PersonalRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_workout_logger.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance

                // Seed initial data if empty
                CoroutineScope(Dispatchers.IO).launch {
                    seedInitialDataIfEmpty(instance)
                }

                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialDataIfEmpty(database)
                    }
                }
            }
        }

        private suspend fun seedInitialDataIfEmpty(database: AppDatabase) {
            val exerciseDao = database.exerciseDao()
            val prDao = database.personalRecordDao()

            if (exerciseDao.getExerciseById(1) == null) {
                val initialExercises = listOf(
                    Exercise(id = 1, name = "Barbell Bench Press", category = "Chest", notes = "Compound upper body push exercise"),
                    Exercise(id = 2, name = "Incline Dumbbell Press", category = "Chest", notes = "Targets upper pectoral muscles"),
                    Exercise(id = 3, name = "Barbell Squat", category = "Legs", notes = "Compound lower body exercise"),
                    Exercise(id = 4, name = "Romanian Deadlift", category = "Legs", notes = "Targets hamstrings and glutes"),
                    Exercise(id = 5, name = "Barbell Deadlift", category = "Back", notes = "Posterior chain compound lift"),
                    Exercise(id = 6, name = "Lat Pulldown", category = "Back", notes = "Upper back and latissimus dorsi"),
                    Exercise(id = 7, name = "Overhead Shoulder Press", category = "Shoulders", notes = "Barbell shoulder push exercise"),
                    Exercise(id = 8, name = "Dumbbell Lateral Raise", category = "Shoulders", notes = "Isolation for side deltoids"),
                    Exercise(id = 9, name = "Barbell Bicep Curl", category = "Arms", notes = "Bicep isolation lift"),
                    Exercise(id = 10, name = "Tricep Rope Pushdown", category = "Arms", notes = "Tricep isolation exercise"),
                    Exercise(id = 11, name = "Plank", category = "Core", notes = "Isometric core stability exercise")
                )
                initialExercises.forEach { exerciseDao.insert(it) }
            }

            if (prDao.getPrForExercise(1) == null) {
                val now = System.currentTimeMillis()
                prDao.insertOrUpdate(PersonalRecord(exerciseId = 1L, maxWeightKg = 100.0, maxReps = 5, achievedTimestamp = now))
                prDao.insertOrUpdate(PersonalRecord(exerciseId = 3L, maxWeightKg = 140.0, maxReps = 3, achievedTimestamp = now))
                prDao.insertOrUpdate(PersonalRecord(exerciseId = 5L, maxWeightKg = 180.0, maxReps = 1, achievedTimestamp = now))
                prDao.insertOrUpdate(PersonalRecord(exerciseId = 7L, maxWeightKg = 65.0, maxReps = 5, achievedTimestamp = now))
            }
        }
    }
}
