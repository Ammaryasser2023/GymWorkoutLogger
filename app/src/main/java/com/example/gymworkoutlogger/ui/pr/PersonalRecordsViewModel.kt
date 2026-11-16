package com.example.gymworkoutlogger.ui.pr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.repository.GymRepository
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PersonalRecordsViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val personalRecords: StateFlow<List<PersonalRecordWithExercise>> =
        repository.getAllPersonalRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}