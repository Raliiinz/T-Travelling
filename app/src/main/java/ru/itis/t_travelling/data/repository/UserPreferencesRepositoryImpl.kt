package ru.itis.t_travelling.data.repository

import android.content.SharedPreferences
import ru.itis.t_travelling.util.Constants
import javax.inject.Inject
import androidx.core.content.edit
import ru.itis.t_travelling.domain.repository.UserPreferencesRepository

class UserPreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
): UserPreferencesRepository {

    override fun saveLoginState(isLoggedIn: Boolean, phone: String?) {
        sharedPreferences.edit() {
            putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(Constants.KEY_USER_PHONE, phone)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
    }

    override fun getLoggedInUserPhone(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_PHONE, null)
    }
}