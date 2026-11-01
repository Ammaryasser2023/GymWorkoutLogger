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
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialExercises(database.exerciseDao())
                    }
                }
            }

            suspend fun populateInitialExercises(exerciseDao: ExerciseDao) {
                val initialExercises = listOf(
                    Exercise(name = "Barbell Bench Press", category = "Chest", notes = "Compound upper body push exercise"),
                    Exercise(name = "Incline Dumbbell Press", category = "Chest", notes = "Targets upper pectoral muscles"),
                    Exercise(name = "Barbell Squat", category = "Legs", notes = "Compound lower body exercise"),
                    Exercise(name = "Romanian Deadlift", category = "Legs", notes = "Targets hamstrings and glutes"),
                    Exercise(name = "Barbell Deadlift", category = "Back", notes = "Posterior chain compound lift"),
                    Exercise(name = "Lat Pulldown", category = "Back", notes = "Upper back and latissimus dorsi"),
                    Exercise(name = "Overhead Shoulder Press", category = "Shoulders", notes = "Barbell shoulder push exercise"),
                    Exercise(name = "Dumbbell Lateral Raise", category = "Shoulders", notes = "Isolation for side deltoids"),
                    Exercise(name = "Barbell Bicep Curl", category = "Arms", notes = "Bicep isolation lift"),
                    Exercise(name = "Tricep Rope Pushdown", category = "Arms", notes = "Tricep isolation exercise"),
                    Exercise(name = "Plank", category = "Core", notes = "Isometric core stability exercise")
                )
                initialExercises.forEach { exerciseDao.insert(it) }
            }
        }
    }
}
