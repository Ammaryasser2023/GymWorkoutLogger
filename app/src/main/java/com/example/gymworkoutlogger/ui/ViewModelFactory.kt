package com.example.gymworkoutlogger.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymworkoutlogger.data.database.AppDatabase
import com.example.gymworkoutlogger.data.repository.GymRepository
import com.example.gymworkoutlogger.data.repository.GymRepositoryImpl
import com.example.gymworkoutlogger.ui.dashboard.DashboardViewModel

class ViewModelFactory(private val repository: GymRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
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
            return ViewModelFactory(repository)
        }
    }
}
