package com.example.gymworkoutlogger.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymworkoutlogger.data.database.AppDatabase
import com.example.gymworkoutlogger.data.repository.GymRepository
import com.example.gymworkoutlogger.data.repository.GymRepositoryImpl
import com.example.gymworkoutlogger.ui.dashboard.DashboardViewModel
import com.example.gymworkoutlogger.ui.exercise.ExerciseViewModel
import com.example.gymworkoutlogger.ui.history.HistoryViewModel
import com.example.gymworkoutlogger.ui.profile.ProfileViewModel

class ViewModelFactory(
    private val repository: GymRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExerciseViewModel::class.java) -> {
                ExerciseViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            val database = AppDatabase.getDatabase(context.applicationContext)
            val repository = GymRepositoryImpl(
                database.exerciseDao(),
                database.workoutSessionDao(),
                database.workoutSetDao(),
                database.personalRecordDao()
            )
            return ViewModelFactory(repository, context.applicationContext)
        }
    }
}
