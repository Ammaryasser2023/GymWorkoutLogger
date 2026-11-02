package com.example.gymworkoutlogger.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import com.example.gymworkoutlogger.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: GymRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow<Long?>(null)
    val selectedDateFilter: StateFlow<Long?> = _selectedDateFilter.asStateFlow()

    val filteredSessions: StateFlow<List<WorkoutSessionWithSets>> = combine(
        repository.getAllSessionsWithSets(),
        _searchQuery,
        _selectedDateFilter
    ) { sessions, query, dateFilter ->
        sessions.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.session.title.contains(query, ignoreCase = true) ||
                    item.session.notes.contains(query, ignoreCase = true)

            val matchesDate = dateFilter == null || item.session.startTime >= dateFilter

            matchesQuery && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(timestamp: Long?) {
        _selectedDateFilter.value = timestamp
    }

    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun getSessionWithSets(sessionId: Long): StateFlow<WorkoutSessionWithSets?> {
        return repository.getSessionWithSets(sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}
