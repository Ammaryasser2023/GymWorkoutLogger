// Member 3: Workout Logger ViewModel
package com.example.gymworkoutlogger.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import com.example.gymworkoutlogger.data.repository.GymRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseWithLoggedSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

class WorkoutLoggerViewModel(private val repository: GymRepository) : ViewModel() {

    private val _currentSessionId = MutableStateFlow<Long>(-1L)
    val currentSessionId: StateFlow<Long> = _currentSessionId.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    val currentSessionWithSets: StateFlow<WorkoutSessionWithSets?> = combine(
        _currentSessionId,
        repository.getAllSessionsWithSets()
    ) { sessionId, sessions ->
        sessions.find { it.session.id == sessionId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeExercisesGrouped: StateFlow<List<ExerciseWithLoggedSets>> = combine(
        currentSessionWithSets,
        repository.getAllExercises()
    ) { sessionWithSets, exercises ->
        if (sessionWithSets == null) emptyList()
        else {
            val sets = sessionWithSets.sets
            val groupedByExerciseId = sets.groupBy { it.exerciseId }
            groupedByExerciseId.mapNotNull { (exerciseId, setList) ->
                val exercise = exercises.find { it.id == exerciseId }
                if (exercise != null) {
                    ExerciseWithLoggedSets(exercise, setList.sortedBy { it.setNumber })
                } else null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadOrStartSession(sessionId: Long) {
        viewModelScope.launch {
            if (sessionId > 0) {
                _currentSessionId.value = sessionId
            } else {
                val active = repository.getActiveSession()
                if (active != null) {
                    _currentSessionId.value = active.id
                } else {
                    val newId = repository.startNewSession("Push Workout Session")
                    _currentSessionId.value = newId
                }
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val session = currentSessionWithSets.value?.session
                if (session != null) {
                    val now = System.currentTimeMillis()
                    val seconds = (now - session.startTime) / 1000
                    _elapsedSeconds.value = if (seconds > 0) seconds else 0
                }
                delay(1000)
            }
        }
    }

    fun addExerciseToWorkout(exercise: Exercise) {
        val sessionId = _currentSessionId.value
        if (sessionId <= 0) return

        viewModelScope.launch {
            val currentSets = currentSessionWithSets.value?.sets?.filter { it.exerciseId == exercise.id } ?: emptyList()
            val nextSetNum = currentSets.size + 1
            val newSet = WorkoutSet(
                workoutSessionId = sessionId,
                exerciseId = exercise.id,
                setNumber = nextSetNum,
                weightKg = 60.0,
                reps = 10,
                isCompleted = false
            )
            repository.logSet(newSet)
        }
    }

    fun addSetToExercise(exerciseId: Long) {
        val sessionId = _currentSessionId.value
        if (sessionId <= 0) return

        viewModelScope.launch {
            val currentSets = currentSessionWithSets.value?.sets?.filter { it.exerciseId == exerciseId } ?: emptyList()
            val nextSetNum = currentSets.size + 1
            val lastWeight = currentSets.lastOrNull()?.weightKg ?: 60.0
            val lastReps = currentSets.lastOrNull()?.reps ?: 10

            val newSet = WorkoutSet(
                workoutSessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = nextSetNum,
                weightKg = lastWeight,
                reps = lastReps,
                isCompleted = false
            )
            repository.logSet(newSet)
        }
    }

    fun updateSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    fun finishSession(onFinished: () -> Unit) {
        val sessionWithSets = currentSessionWithSets.value
        if (sessionWithSets != null) {
            viewModelScope.launch {
                repository.finishSession(sessionWithSets.session)
                timerJob?.cancel()
                onFinished()
            }
        }
    }

    fun cancelSession(onCancelled: () -> Unit) {
        val sessionWithSets = currentSessionWithSets.value
        if (sessionWithSets != null) {
            viewModelScope.launch {
                repository.deleteSession(sessionWithSets.session)
                timerJob?.cancel()
                onCancelled()
            }
        }
    }
}
