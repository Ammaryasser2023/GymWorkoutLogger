// Member 5: Profile & Personal Records ViewModel
package com.example.gymworkoutlogger.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import com.example.gymworkoutlogger.data.repository.GymRepository
import com.example.gymworkoutlogger.data.user.UserProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserActivityStats(
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val totalVolumeKg: Int = 0,
    val activeDaysCount: Int = 0,
    val topExercisesText: String = "No exercise data available yet."
)

class ProfileViewModel(
    private val repository: GymRepository,
    context: Context
) : ViewModel() {

    private val userProfileManager = UserProfileManager(context)

    private val _userName = MutableStateFlow(userProfileManager.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _profileImageUri = MutableStateFlow(userProfileManager.profileImageUri)
    val profileImageUri: StateFlow<String?> = _profileImageUri.asStateFlow()

    val personalRecords: StateFlow<List<PersonalRecordWithExercise>> =
        repository.getAllPersonalRecords()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityStats: StateFlow<UserActivityStats> = combine(
        repository.getAllSessionsWithSets(),
        repository.getAllExercises()
    ) { sessionsWithSets, exercises ->
        val completedSessions = sessionsWithSets.filter { it.session.endTime != null || it.sets.isNotEmpty() }

        val totalWorkouts = completedSessions.size
        val allSets = completedSessions.flatMap { it.sets }
        val totalSets = allSets.size
        val totalVolumeKg = allSets.sumOf { it.weightKg * it.reps }.toInt()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val activeDaysCount = completedSessions.map { dateFormat.format(Date(it.session.startTime)) }.distinct().size

        val exerciseMap = exercises.associateBy { it.id }
        val setsByExercise = allSets.groupBy { it.exerciseId }
        val sortedTopExercises = setsByExercise.entries
            .sortedByDescending { it.value.size }
            .take(3)
            .mapNotNull { (exerciseId, setList) ->
                val name = exerciseMap[exerciseId]?.name
                if (name != null) "• $name (${setList.size} sets)" else null
            }

        val topExercisesText = if (sortedTopExercises.isNotEmpty()) {
            sortedTopExercises.joinToString("\n")
        } else {
            "No exercise data available yet."
        }

        UserActivityStats(
            totalWorkouts = totalWorkouts,
            totalSets = totalSets,
            totalVolumeKg = totalVolumeKg,
            activeDaysCount = activeDaysCount,
            topExercisesText = topExercisesText
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserActivityStats())

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            userProfileManager.userName = trimmed
            _userName.value = trimmed
        }
    }

    fun updateProfileImageUri(uriString: String?) {
        userProfileManager.profileImageUri = uriString
        _profileImageUri.value = uriString
    }
}
