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
import kotlinx.coroutines.flow.stateIn

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
