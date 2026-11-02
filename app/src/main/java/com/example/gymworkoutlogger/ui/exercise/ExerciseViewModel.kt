package com.example.gymworkoutlogger.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repository: GymRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExercises: StateFlow<List<Exercise>> = combine(
        repository.getAllExercises(),
        _searchQuery,
        _selectedCategory
    ) { exercises, query, category ->
        exercises.filter { exercise ->
            val matchesQuery = query.isEmpty() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.notes.contains(query, ignoreCase = true)

            val matchesCategory = category.isNullOrEmpty() ||
                    category.equals("All", ignoreCase = true) ||
                    exercise.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun saveExercise(id: Long, name: String, category: String, notes: String) {
        viewModelScope.launch {
            val exercise = Exercise(
                id = if (id > 0) id else 0,
                name = name,
                category = category,
                notes = notes
            )
            if (id > 0) {
                repository.updateExercise(exercise)
            } else {
                repository.insertExercise(exercise)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }
}
