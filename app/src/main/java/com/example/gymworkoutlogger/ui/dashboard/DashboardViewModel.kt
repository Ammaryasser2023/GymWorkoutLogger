package com.example.gymworkoutlogger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import com.example.gymworkoutlogger.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: GymRepository) : ViewModel() {

    val recentSessions: StateFlow<List<WorkoutSessionWithSets>> = repository.getAllSessionsWithSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecordWithExercise>> = repository.getAllPersonalRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession: StateFlow<WorkoutSession?> = _activeSession.asStateFlow()

    init {
        checkActiveSession()
    }

    fun checkActiveSession() {
        viewModelScope.launch {
            _activeSession.value = repository.getActiveSession()
        }
    }

    fun startNewSession(title: String, onSessionCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = repository.startNewSession(title)
            checkActiveSession()
            onSessionCreated(sessionId)
        }
    }
}
