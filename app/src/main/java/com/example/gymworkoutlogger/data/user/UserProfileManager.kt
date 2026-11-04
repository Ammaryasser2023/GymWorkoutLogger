package com.example.gymworkoutlogger.data.user

import android.content.Context
import android.content.SharedPreferences

class UserProfileManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Athlete") ?: "Athlete"
        set(value) {
            prefs.edit().putString(KEY_USER_NAME, value).apply()
        }

    var profileImageUri: String?
        get() = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        set(value) {
            prefs.edit().putString(KEY_PROFILE_IMAGE_URI, value).apply()
        }

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PROFILE_IMAGE_URI = "key_profile_image_uri"
    }
}
