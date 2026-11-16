
package com.example.gymworkoutlogger.ui.pr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActivityStatsUiState(
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val activeDays: Int = 0,
    val weeklyActivity: List<DailyActivity> = emptyList(),
    val mostTrainedExercises: List<ExerciseActivity> = emptyList()
)

data class DailyActivity(
    val day: String,
    val workoutCount: Int
)

data class ExerciseActivity(
    val exerciseName: String,
    val setCount: Int
)

class ActivityStatsViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val stats: StateFlow<ActivityStatsUiState> =
        combine(
            repository.getAllSessionsWithSets(),
            repository.getAllExercises()
        ) { sessionsWithSets, exercises ->

            val completedSessions = sessionsWithSets.filter { sessionWithSets ->
                sessionWithSets.session.endTime != null ||
                        sessionWithSets.sets.any { it.isCompleted }
            }

            val completedSets = completedSessions.flatMap { sessionWithSets ->
                sessionWithSets.sets.filter { it.isCompleted }
            }

            val totalVolume = completedSets.sumOf { set ->
                set.weightKg * set.reps
            }

            val activeDays = completedSessions
                .map { sessionWithSets ->
                    dateKey(sessionWithSets.session.startTime)
                }
                .distinct()
                .size

            val weeklyActivity = getLastSevenDaysActivity(completedSessions)

            val exerciseCounts = completedSets
                .groupingBy { it.exerciseId }
                .eachCount()

            val exerciseNames = exercises.associateBy { it.id }

            val mostTrainedExercises = exerciseCounts
                .mapNotNull { (exerciseId, count) ->
                    exerciseNames[exerciseId]?.let { exercise ->
                        ExerciseActivity(
                            exerciseName = exercise.name,
                            setCount = count
                        )
                    }
                }
                .sortedByDescending { it.setCount }
                .take(5)

            ActivityStatsUiState(
                totalWorkouts = completedSessions.size,
                totalSets = completedSets.size,
                totalVolume = totalVolume,
                activeDays = activeDays,
                weeklyActivity = weeklyActivity,
                mostTrainedExercises = mostTrainedExercises
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityStatsUiState()
        )

    private fun getLastSevenDaysActivity(
        sessions: List<com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets>
    ): List<DailyActivity> {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        return (0..6).map {
            val date = calendar.time
            val dateKey = dateFormat.format(date)

            val count = sessions.count { sessionWithSets ->
                dateFormat.format(Date(sessionWithSets.session.startTime)) == dateKey
            }

            val activity = DailyActivity(
                day = dayFormat.format(date),
                workoutCount = count
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            activity
        }
    }

    private fun dateKey(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(timestamp))
    }
}
